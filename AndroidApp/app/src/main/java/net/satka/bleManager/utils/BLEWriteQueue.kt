package net.satka.bleManager.utils
import java.util.LinkedList
import java.util.Queue
import java.util.UUID

class BLEWriteQueue(private val onRequestWrite: ((serviceUUID: UUID, characteristicUUID: UUID, value: ByteArray) -> Boolean)) {
    private val writeQueue: Queue<WriteRequest> = LinkedList()
    private var isWriting = false

    class WriteRequest(
        val serviceUUID: UUID,
        val characteristicUUID: UUID,
        val value: ByteArray
    )

    private fun processNextWrite() {
        if (isWriting || writeQueue.isEmpty()) return

        val request = writeQueue.poll() ?: return
        isWriting = true

        val success = onRequestWrite.invoke(request.serviceUUID, request.characteristicUUID, request.value)

        if (success != true) {
            isWriting = false
            processNextWrite()
        }
    }

    fun enqueueWrite(serviceUUID: UUID, characteristicUUID: UUID, value: ByteArray) {
        writeQueue.add(WriteRequest(serviceUUID, characteristicUUID, value))
        processNextWrite()
    }

    fun onWriteComplete() {
        isWriting = false
        processNextWrite()
    }
}
