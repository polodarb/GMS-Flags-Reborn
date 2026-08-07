package ua.polodarb.gmsflags.data.phenotype.root.connector

import android.content.Intent
import android.os.IBinder
import com.topjohnwu.superuser.ipc.RootService
import ua.polodarb.gmsflags.data.phenotype.root.IPhenotypeRootService

class PhenotypeRootService : RootService() {
    private val operations: PhenotypeRootOperations by lazy {
        PhenotypeRootOperationsFactory(this).create()
    }

    override fun onBind(intent: Intent): IBinder = PhenotypeRootBinder(operations)
}
