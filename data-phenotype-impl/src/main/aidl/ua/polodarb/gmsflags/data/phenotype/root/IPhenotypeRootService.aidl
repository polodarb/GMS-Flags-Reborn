package ua.polodarb.gmsflags.data.phenotype.root;

import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypePackageBindingParcel;
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagParcel;
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagPageParcel;
import ua.polodarb.gmsflags.data.phenotype.root.parcel.HookDiagnosticSnapshotParcel;
import ua.polodarb.gmsflags.data.phenotype.root.parcel.XposedScopeSnapshotParcel;
import ua.polodarb.gmsflags.data.phenotype.root.parcel.MicroHookEnvelopeParcel;

interface IPhenotypeRootService {
    List<PhenotypePackageBindingParcel> readPhenotypePackages();
    PhenotypeFlagPageParcel readFlagsPage(
        String androidPackageName,
        String phenotypePackageName,
        int offset,
        int limit
    );
    void writeOverrides(String androidPackageName, String phenotypePackageName, in List<PhenotypeFlagParcel> overrides);
    void writeMicroHooks(String androidPackageName, in List<MicroHookEnvelopeParcel> hooks);
    void deleteMicroHooks(String androidPackageName, in long[] recipeIds);
    void deleteOverride(String androidPackageName, String phenotypePackageName, String flagName);
    void deleteOverrides(String androidPackageName, String phenotypePackageName, in List<String> flagNames);
    void deletePackageOverrides(String androidPackageName, String phenotypePackageName);
    int readOverrideCount(in List<String> androidPackageNames);
    boolean readOverridesPaused(in List<String> androidPackageNames);
    void setOverridesPaused(in List<String> androidPackageNames, boolean paused);
    void deleteAllOverrides(in List<String> androidPackageNames);
    List<HookDiagnosticSnapshotParcel> readHookDiagnostics(in List<String> androidPackageNames);
    XposedScopeSnapshotParcel readXposedScope(String modulePackageName, int userId);
    String readXposedLogs(String androidPackageName);
}
