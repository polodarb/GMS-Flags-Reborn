package ua.polodarb.xposed.hook.strategy.inputmethod

import ua.polodarb.xposed.hook.strategy.common.ObjectMethodNames
import java.lang.reflect.Method

internal object InputMethodFlagReflection {
    fun findNameAccessor(valueGetter: Method): Method? =
        valueGetter.declaringClass.declaredMethods.filter { method ->
            method.parameterCount == 0 &&
                method.returnType == String::class.java &&
                method.name != ObjectMethodNames.TO_STRING
        }.singleOrNull()
}
