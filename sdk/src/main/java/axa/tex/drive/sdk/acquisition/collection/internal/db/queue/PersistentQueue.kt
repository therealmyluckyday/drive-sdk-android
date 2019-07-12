package axa.tex.drive.sdk.acquisition.collection.internal.db.queue

import android.content.Context
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import org.koin.android.ext.android.inject
import java.io.*
import java.nio.file.Files.isDirectory



 private const val SEP : String  = "$"

class PersistentQueue : KoinComponentCallbacks{

    var  context : Context
    private var collectorDb: CollectionDb? = null

    constructor(context: Context){
        this.context = context

        val collectorDb: CollectionDb by inject()
        this.collectorDb = collectorDb
    }

    private fun buildFileName(appName : String, clientId : String) : String{
        return "$appName$SEP$clientId"
    }

    @Synchronized internal fun enqueue(tripId : String, appName : String, clientId : String, data : String, endOftTrip: Boolean){

        try {
            val rootPath = context.filesDir
                    .absolutePath + "/TEX/$tripId/"
            val root = File(rootPath)
            if (!root.exists()) {
                root.mkdirs()
            }

            //val packetNumber = collectorDb?.getPacketNumber(tripId)
            val number = collectorDb?.getNumberPackets(tripId)
           //val number = root.list().size+1
            val f = File(rootPath + "$tripId$SEP${buildFileName(appName,clientId)}$SEP$number$SEP$endOftTrip")
            if (f.exists()) {
                f.delete()
            }
            f.createNewFile()

            val out = FileOutputStream(f)
            out.write(data.toByteArray())

            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readFromFileInputStream(fileInputStream: FileInputStream?): String {
        val retBuf = StringBuffer()

        try {
            if (fileInputStream != null) {
                val inputStreamReader = InputStreamReader(fileInputStream)
                val bufferedReader = BufferedReader(inputStreamReader)

                var lineData = bufferedReader.readLine()
                while (lineData != null) {
                    retBuf.append(lineData)
                    lineData = bufferedReader.readLine()
                }
                return retBuf.toString()
            }
        } catch (ex: IOException) {
            return ""
        }
        return ""
    }

    internal fun pop(tripId : String, appName : String, clientId: String, packetNumber : String, endOftTrip: Boolean):PersistablePacket?{
        try {
            val rootPath = context.filesDir
                    .absolutePath + "/TEX/$tripId/"
            val file = File(rootPath, "$tripId$SEP${buildFileName(appName, clientId)}$SEP$packetNumber$SEP$endOftTrip")
            val fin = FileInputStream(file)
            val data = readFromFileInputStream(fin)
            fin.close()
            return PersistablePacket(tripId, appName, clientId, data, packetNumber.toInt(), endOftTrip)
        }catch (e : Exception){
            return null
        }
    }

    internal fun deleteTrip(tripId : String) : Boolean{
        val rootPath = context.filesDir
                .absolutePath + "/TEX/$tripId/"
        val dir = File(rootPath)
        deleteRecursive(dir)
        return dir.exists()
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory)
            for (child in fileOrDirectory.listFiles())
                deleteRecursive(child)
        fileOrDirectory.delete()
    }

    /*fun listTrips() : List<PersistablePacket>{
        val persistablePackets = mutableListOf<PersistablePacket>()
        val rootPath = context.filesDir
                .absolutePath + "/TEX/"
        val root = File(rootPath)
        val files = root.list()
        for(file in files){
            var tab = file.split(SEP)
            if(tab.size >= 3){
                val tripId = tab[0]
                val appName = tab[1]
                val clientId = tab[2]
                val packet = pop(tripId, appName, clientId)
                persistablePackets.add(packet)
            }
        }
        return persistablePackets
    }*/

    private fun listTrips(tripId: String) : List<PersistablePacket>{
        val persistablePackets = mutableListOf<PersistablePacket>()
        val rootPath = context.filesDir
                .absolutePath + "/TEX/$tripId"
        val root = File(rootPath)
        val files = root.list()
        for(file in files){
            var tab = file.split(SEP)
            if(tab.size >= 3){
                val tripId = tab[0]
                val appName = tab[1]
                val clientId = tab[2]
                val packetNumber = tab[3]
                val endOftTrip = tab[4].toBoolean()
                val packet = pop(tripId, appName, clientId, packetNumber, endOftTrip)
                if(packet != null) {
                    persistablePackets.add(packet)
                }
            }
        }
        return persistablePackets
    }

    internal fun tripExists(tripId: String): Boolean{
        val rootPath = context.filesDir
                .absolutePath + "/TEX/$tripId"
        val root = File(rootPath)
        return root.exists()
    }

    internal fun next(tripId : String) : PersistablePacket?{

        val packets = listTrips(tripId)
        if(packets != null && packets.isNotEmpty()){
            return packets.sorted().first()
        }
        return null
    }


    internal fun delete(packet: PersistablePacket){

        try {
            val rootPath = context.filesDir
                    .absolutePath + "/TEX/${packet.tripId}/"
            val root = File(rootPath)
            if (!root.exists()) {
                root.mkdirs()
            }
            val number = packet.packetNumber
            val appName : String = packet.appName!!
            val clientId : String = packet.clientId!!
            val f = File(rootPath + "${packet.tripId}$SEP${buildFileName(appName,clientId)}$SEP$number$SEP${packet.end}")
            if (f.exists()) {
                f.delete()
            }

        }catch (e : Exception){
            e.printStackTrace()
        }
        }

    fun numberOfPendingPacket(tripId: String) : Int{
        try {
            val rootPath = context.filesDir
                    .absolutePath + "/TEX/$tripId/"
            val root = File(rootPath)
            if (!root.exists()) {
                root.mkdirs()
            }
           return root.list().size
        }catch (e : Exception){
            e.printStackTrace()
        }
        return 0
    }




    }