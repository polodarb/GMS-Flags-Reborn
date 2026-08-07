package ua.polodarb.xposed.info

data class PhenotypePackageCandidate(
    val packageName: String,
    val source: String,
)

fun phenotypePackageCandidates(
    identityPackageName: String,
    contextPackageName: String,
): List<PhenotypePackageCandidate> = buildList {
    add(PhenotypePackageCandidate(identityPackageName, "identity-package"))

    if ('#' in identityPackageName) {
        add(
            PhenotypePackageCandidate(
                identityPackageName.substringBefore('#'),
                "identity-base-package",
            )
        )
    } else {
        add(
            PhenotypePackageCandidate(
                "$identityPackageName#$contextPackageName",
                "identity-context-package",
            )
        )
    }

    add(PhenotypePackageCandidate(contextPackageName, "context-package"))
}.distinctBy(PhenotypePackageCandidate::packageName)
