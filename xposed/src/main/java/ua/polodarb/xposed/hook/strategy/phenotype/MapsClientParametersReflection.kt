package ua.polodarb.xposed.hook.strategy.phenotype

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.result.FieldData
import org.luckypray.dexkit.result.MethodData
import org.luckypray.dexkit.result.UsingFieldData
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore
import ua.polodarb.xposed.store.RuntimeFlagOverrideValueParser

internal class MapsClientParametersReflection private constructor(
    private val converterConstructor: Constructor<*>,
    private val converterMethod: Method,
    private val converterIncludeDefaultsField: Field,
    private val snapshotConstructor: Constructor<*>,
    private val snapshotRecordsField: Field,
    private val recordConstructor: Constructor<*>,
    private val recordPresenceFields: List<Field>,
    private val recordDiscriminatorField: Field,
    private val recordNameField: Field,
    private val recordValueField: Field,
    private val wrapperClass: Class<*>,
    private val wrapperGroupFieldCandidates: List<Field>,
    val groupAccessor: Method,
    private val groupMapField: Field,
) {
    val converterDescription: String
        get() = "${converterMethod.declaringClass.name}#${converterMethod.name}"

    fun buildOverlays(
        matches: Map<String, RuntimeFlagOverrideStore.Match>,
        phenotypePackageName: String,
    ): OverlayResult {
        val numericOverrides = matches.mapNotNull { (flagName, match) ->
            val flagId = flagName.toIntOrNull() ?: return@mapNotNull null
            val override = match.override ?: return@mapNotNull null
            val value = RuntimeFlagOverrideValueParser.parse(null, override)
                ?: return@mapNotNull null
            val discriminator = value.discriminatorOrNull() ?: return@mapNotNull null
            NumericOverride(
                flagId = flagId,
                identity = "$phenotypePackageName/$flagName",
                value = value,
                discriminator = discriminator,
            )
        }
        if (numericOverrides.isEmpty()) {
            return OverlayResult(emptyList(), emptyList(), wrapperGroupFieldCandidates.first())
        }

        val selectedGroups = convert(numericOverrides)
        val baselineGroups = convert(emptyList())
        val innerField = wrapperClass.declaredFields.singleOrNull { field ->
            !Modifier.isStatic(field.modifiers) && field.type == Any::class.java
        }?.apply { isAccessible = true }
            ?: throw IllegalStateException("Maps parameter-group value field not found")

        val baselineByValueClass = baselineGroups.mapNotNull { wrapper ->
            innerField.getOrNull(wrapper)?.javaClass?.let { it to wrapper }
        }.toMap()
        val changedGroups = selectedGroups.filter { wrapper ->
            val valueClass = innerField.getOrNull(wrapper)?.javaClass ?: return@filter false
            baselineByValueClass[valueClass] != wrapper
        }
        val groupField = wrapperGroupFieldCandidates.maxByOrNull { field ->
            selectedGroups.mapNotNull { field.getIntOrNull(it) }.distinct().size
        } ?: throw IllegalStateException("Maps parameter-group id field not found")

        return OverlayResult(
            numericOverrides = numericOverrides,
            changedGroups = changedGroups,
            groupIdField = groupField,
        )
    }

    fun mergeGroups(
        originalGroups: List<*>,
        overlays: OverlayResult,
    ): GroupListMergeResult {
        val overlayByGroupId = overlays.changedGroups.associateBy { wrapper ->
            overlays.groupIdField.getInt(wrapper)
        }
        val seenGroupIds = mutableSetOf<Int>()
        var mergedGroupCount = 0
        val groups = ArrayList<Any?>(originalGroups.size + overlayByGroupId.size)

        originalGroups.forEach { original ->
            if (original == null || !wrapperClass.isInstance(original)) {
                groups += original
                return@forEach
            }
            val groupId = overlays.groupIdField.getInt(original)
            val overlay = overlayByGroupId[groupId]
            if (overlay == null) {
                groups += original
                return@forEach
            }
            seenGroupIds += groupId
            groups += mergeMessage(original, overlay)
            mergedGroupCount++
        }

        var appendedGroupCount = 0
        overlayByGroupId.forEach { (groupId, overlay) ->
            if (groupId !in seenGroupIds) {
                groups += overlay
                appendedGroupCount++
            }
        }
        return GroupListMergeResult(
            groups = groups,
            stats = MergeStats(
                mergedGroupCount = mergedGroupCount + appendedGroupCount,
                appendedGroupCount = appendedGroupCount,
            ),
        )
    }

    fun mergeIntoInstance(
        instance: Any,
        overlays: OverlayResult,
    ): MergeStats {
        @Suppress("UNCHECKED_CAST")
        val groupMap = groupMapField.get(instance) as? MutableMap<Any?, Any?>
            ?: throw IllegalStateException("Maps ClientParameters group map is not mutable")
        val overlayByGroupId = overlays.changedGroups.associateBy { wrapper ->
            overlays.groupIdField.getInt(wrapper)
        }
        var mergedGroupCount = 0
        synchronized(instance) {
            groupMap.entries.forEach { entry ->
                val original = entry.value
                if (original == null || !wrapperClass.isInstance(original)) return@forEach
                val groupId = overlays.groupIdField.getInt(original)
                val overlay = overlayByGroupId[groupId] ?: return@forEach
                entry.setValue(mergeMessage(original, overlay))
                mergedGroupCount++
            }
        }
        return MergeStats(
            mergedGroupCount = mergedGroupCount,
            appendedGroupCount = 0,
        )
    }

    private fun convert(overrides: List<NumericOverride>): List<Any> {
        val records = overrides.map(::newRecord)
        val snapshot = newSnapshot(records)
        val converter = converterConstructor.newInstance()
        converterIncludeDefaultsField.setBoolean(converter, true)
        val result = converterMethod.invoke(converter, snapshot) as? Iterable<*>
            ?: throw IllegalStateException("Maps converter returned a non-iterable value")
        return result.mapNotNull { item ->
            when {
                item == null -> null
                wrapperClass.isInstance(item) -> item
                else -> item.javaClass.methods.firstOrNull { method ->
                    method.name == "build" && method.parameterCount == 0
                }?.invoke(item)?.takeIf(wrapperClass::isInstance)
            }
        }
    }

    private fun newRecord(override: NumericOverride): Any =
        recordConstructor.newInstance().also { record ->
            recordPresenceFields.forEach { field -> field.setInt(record, FLAG_NAME_PRESENT_MASK) }
            recordDiscriminatorField.setInt(record, override.discriminator)
            recordNameField.set(record, override.flagId.toString())
            recordValueField.set(record, override.value)
        }

    private fun newSnapshot(records: List<Any>): Any =
        snapshotConstructor.newInstance().also { snapshot ->
            val emptyList = snapshotRecordsField.get(snapshot)
            val mutableCopyMethod = emptyList.javaClass.methods.single { method ->
                !Modifier.isStatic(method.modifiers) &&
                    method.parameterTypes.contentEquals(
                        arrayOf(Int::class.javaPrimitiveType),
                    ) &&
                    List::class.java.isAssignableFrom(method.returnType)
            }.apply { isAccessible = true }
            @Suppress("UNCHECKED_CAST")
            val mutableList = mutableCopyMethod.invoke(
                emptyList,
                records.size.coerceAtLeast(MIN_MUTABLE_LIST_CAPACITY),
            ) as MutableList<Any>
            mutableList.addAll(records)
            snapshotRecordsField.set(snapshot, mutableList)
        }

    private fun mergeMessage(original: Any, overlay: Any): Any {
        val toBuilder = original.javaClass.methods.firstOrNull { method ->
            method.name == "toBuilder" && method.parameterCount == 0
        } ?: throw IllegalStateException("Maps parameter-group toBuilder method not found")
        val builder = toBuilder.invoke(original)
        val mergeFrom = builder.javaClass.methods.firstOrNull { method ->
            method.name == "mergeFrom" &&
                method.parameterCount == 1 &&
                method.parameterTypes[0].isAssignableFrom(overlay.javaClass)
        } ?: throw IllegalStateException("Maps parameter-group mergeFrom method not found")
        mergeFrom.invoke(builder, overlay)
        val build = builder.javaClass.methods.firstOrNull { method ->
            method.name == "build" && method.parameterCount == 0
        } ?: throw IllegalStateException("Maps parameter-group build method not found")
        return build.invoke(builder)
            ?: throw IllegalStateException("Maps parameter-group build returned null")
    }

    data class NumericOverride(
        val flagId: Int,
        val identity: String,
        val value: Any,
        val discriminator: Int,
    )

    data class OverlayResult(
        val numericOverrides: List<NumericOverride>,
        val changedGroups: List<Any>,
        val groupIdField: Field,
    )

    data class GroupListMergeResult(
        val groups: List<Any?>,
        val stats: MergeStats,
    )

    data class MergeStats(
        val mergedGroupCount: Int,
        val appendedGroupCount: Int,
    )

    companion object {
        fun discover(
            bridge: DexKitBridge,
            classLoader: ClassLoader,
            updateMethod: Method,
        ): MapsClientParametersReflection? = runCatching {
            val converterData = bridge.findMethod {
                matcher { usingStrings(CONVERTER_ANCHOR) }
            }.filter { candidate ->
                Modifier.isPublic(candidate.modifiers) &&
                    !Modifier.isStatic(candidate.modifiers) &&
                    candidate.paramTypes.size == 1 &&
                    candidate.returnTypeName == List::class.java.name
            }.distinctBy(MethodData::descriptor).single()
            val converterMethod = converterData.getMethodInstance(classLoader).apply {
                isAccessible = true
            }
            val converterClass = converterMethod.declaringClass
            val snapshotClass = converterMethod.parameterTypes.single()

            val recordClass = discoverRecordClass(bridge, classLoader)
            val groupAccessor = updateMethod.declaringClass.declaredMethods.single { method ->
                method.name == GROUP_ACCESSOR_NAME &&
                    method.parameterCount == 1 &&
                    method.parameterTypes.single().isEnum
            }.apply { isAccessible = true }
            val wrapperClass = groupAccessor.returnType
            val groupEnumClass = groupAccessor.parameterTypes.single()
            val groupFieldCandidates = discoverGroupFields(
                bridge = bridge,
                converterClass = converterClass,
                groupEnumClass = groupEnumClass,
                wrapperClass = wrapperClass,
                classLoader = classLoader,
            ).ifEmpty {
                wrapperClass.declaredFields.filter { field ->
                    !Modifier.isStatic(field.modifiers) && field.type == Int::class.javaPrimitiveType
                }.onEach { it.isAccessible = true }
            }

            val snapshotRecordsField = converterData.usingFields
                .map(UsingFieldData::field)
                .filter { field -> field.className == snapshotClass.name }
                .map { field -> field.getFieldInstance(classLoader) }
                .filter { field -> List::class.java.isAssignableFrom(field.type) }
                .distinctBy(Field::getName)
                .single()
                .apply { isAccessible = true }
            val recordFields = recordClass.declaredFields.filterNot { Modifier.isStatic(it.modifiers) }
                .onEach { it.isAccessible = true }
            val recordValueField = recordFields.single { it.type == Any::class.java }
            val recordIntFields = recordFields.filter {
                it.type == Int::class.javaPrimitiveType
            }
            val valueFieldIndex = recordFields.indexOf(recordValueField)
            val recordDiscriminatorField = recordIntFields.minBy { field ->
                kotlin.math.abs(recordFields.indexOf(field) - valueFieldIndex)
            }
            MapsClientParametersReflection(
                converterConstructor = converterClass.getDeclaredConstructor().apply {
                    isAccessible = true
                },
                converterMethod = converterMethod,
                converterIncludeDefaultsField = converterClass.declaredFields.single { field ->
                    !Modifier.isStatic(field.modifiers) &&
                        field.type == Boolean::class.javaPrimitiveType
                }.apply { isAccessible = true },
                snapshotConstructor = snapshotClass.getDeclaredConstructor().apply {
                    isAccessible = true
                },
                snapshotRecordsField = snapshotRecordsField,
                recordConstructor = recordClass.getDeclaredConstructor().apply {
                    isAccessible = true
                },
                recordPresenceFields = recordIntFields - recordDiscriminatorField,
                recordDiscriminatorField = recordDiscriminatorField,
                recordNameField = recordFields.single { it.type == String::class.java },
                recordValueField = recordValueField,
                wrapperClass = wrapperClass,
                wrapperGroupFieldCandidates = groupFieldCandidates,
                groupAccessor = groupAccessor,
                groupMapField = updateMethod.declaringClass.declaredFields.single { field ->
                    !Modifier.isStatic(field.modifiers) &&
                        Map::class.java.isAssignableFrom(field.type)
                }.apply { isAccessible = true },
            )
        }.onFailure { error ->
            XposedLogger.logW("Maps ClientParameters reflection discovery failed: ${error.message}")
        }.getOrNull()

        private fun discoverRecordClass(
            bridge: DexKitBridge,
            classLoader: ClassLoader,
        ): Class<*> {
            val candidates = bridge.findMethod {
                matcher { usingStrings(SNAPSHOT_RECORD_ANCHOR) }
            }.flatMap { method: MethodData ->
                method.usingFields.map(UsingFieldData::field)
            }
                .groupBy { field: FieldData -> field.className }
                .keys
                .mapNotNull { className ->
                    runCatching { Class.forName(className, false, classLoader) }.getOrNull()
                }
                .filter { type ->
                    val fields = type.declaredFields.filterNot { Modifier.isStatic(it.modifiers) }
                    fields.count { it.type == Any::class.java } == 1 &&
                        fields.count { it.type == String::class.java } == 1 &&
                        fields.count { it.type == Int::class.javaPrimitiveType } >= 2
                }
                .distinct()
            return candidates.single()
        }

        private fun discoverGroupFields(
            bridge: DexKitBridge,
            converterClass: Class<*>,
            groupEnumClass: Class<*>,
            wrapperClass: Class<*>,
            classLoader: ClassLoader,
        ): List<Field> {
            val candidates = bridge.findMethod {
                matcher {
                    declaredClass(converterClass)
                    paramTypes(groupEnumClass)
                }
            }.filter { candidate ->
                Modifier.isPrivate(candidate.modifiers) && Modifier.isStatic(candidate.modifiers)
            }.map { candidate ->
                candidate.usingFields.map(UsingFieldData::field).filter { field: FieldData ->
                    field.className == wrapperClass.name && field.typeName == "int"
                }
            }.filter { fields: List<FieldData> -> fields.size >= 2 }

            return candidates.singleOrNull().orEmpty()
                .map { field: FieldData ->
                    field.getFieldInstance(classLoader).apply { isAccessible = true }
                }
                .distinctBy(Field::getName)
        }

        private const val CONVERTER_ANCHOR = "Parsing parameter bytes threw an exception"
        private const val SNAPSHOT_RECORD_ANCHOR = "Unrecognized flag type:"
        private const val GROUP_ACCESSOR_NAME = "getGroup"
        private const val MIN_MUTABLE_LIST_CAPACITY = 10
        private const val FLAG_NAME_PRESENT_MASK = 1
    }
}

private fun Any.discriminatorOrNull(): Int? = when (this) {
    is Long -> 1
    is Boolean -> 2
    is Double -> 3
    is String -> 4
    else -> null
}

private fun Field.getOrNull(instance: Any): Any? = runCatching { get(instance) }.getOrNull()

private fun Field.getIntOrNull(instance: Any): Int? = runCatching { getInt(instance) }.getOrNull()
