package com.erikschouten.merkletree

import com.erikschouten.roothash.RootHash
import com.erikschouten.roothash.leaf.Hash
import com.erikschouten.roothash.leaf.SecureData
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.bouncycastle.util.encoders.Base64
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RootHashTests {

    @Test
    fun enableDefaultTypingTest() {
        val tradelaneData = TestTradelane("Hauwert", TestTransporter("Erik", 1))
        val json = jacksonObjectMapper().enableDefaultTyping().writeValueAsString(tradelaneData)
        val obj = jacksonObjectMapper().enableDefaultTyping().readValue<TestTradelane>(json)

        assertEquals(
            tradelaneData,
            obj,
            "Data (de)serialisation gone wrong"
        )
    }

    @Test
    fun jsonConversionTest() {
        val billOfLading = SecureData("data:application/pdf;base64,BillOfLading")
        val commercialInvoice =
            SecureData("data:application/pdf;base64,CommercialInvoice")
        val packingList =
            Hash(SecureData("data:application/pdf;base64,PackingList"))
        val tradelaneData =
            SecureData(TestTradelane("Hauwert", TestTransporter("Erik", 1)))

        val merkleTree = RootHash.build(
            listOf(
                billOfLading,
                commercialInvoice,
                packingList,
                tradelaneData
            )
        )

        val objectmapper = jacksonObjectMapper().enableDefaultTyping()

        val json = objectmapper.writeValueAsString(merkleTree)
        val obj = objectmapper.readValue<RootHash>(json)

        assertEquals(
            merkleTree,
            obj,
            "Data (de)serialisation gone wrong"
        )
    }

    @Test
    fun dataTypeTest() {
        val tradelaneData =
            SecureData(TestTradelane("Hauwert", TestTransporter("Erik", 1)))

        assert(tradelaneData.get() is TestTradelane)
    }

    @Test
    fun evenTreeTest() {
        val billOfLading = SecureData("data:application/pdf;base64,BillOfLading")
        val commercialInvoice =
            SecureData("data:application/pdf;base64,CommercialInvoice")
        val packingList = SecureData("data:application/pdf;base64,PackingList")
        val tradelaneData =
            SecureData(TestTradelane("Hauwert", TestTransporter("Erik", 1)))

        val dataList = listOf(
            billOfLading,
            commercialInvoice,
            packingList,
            tradelaneData
        )

        val merkleTree = RootHash.build(dataList)

        assertEquals(
            dataList.size,
            merkleTree.findData().size,
            "Incorrect/incomplete data found (even tree)"
        )

        assert(dataList.map { it.get() }.containsAll(merkleTree.findData()))
    }

    @Test
    fun unevenTreeTest() {
        val billOfLading = SecureData("data:application/pdf;base64,BillOfLading")
        val commercialInvoice =
            SecureData("data:application/pdf;base64,CommercialInvoice")
        val packingList = SecureData("data:application/pdf;base64,PackingList")
        val tradelaneData =
            SecureData(TestTradelane("Hauwert", TestTransporter("Erik", 1)))
        val waybill = SecureData("data:application/pdf;base64,Waybill")

        val dataList = listOf(
            billOfLading,
            commercialInvoice,
            packingList,
            tradelaneData,
            waybill
        )

        val merkleTree = RootHash.build(dataList)

        assertEquals(
            dataList.size,
            merkleTree.findData().size,
            "Incorrect/incomplete data found (uneven tree)"
        )

        assert(dataList.map { it.get() }.containsAll(merkleTree.findData()))
    }

    @Test
    fun singleNodeTest() {
        val billOfLading = SecureData("data:application/pdf;base64,BillOfLading")
        RootHash.build(listOf(billOfLading))
    }

    @Test
    fun sameDataDifferentNonceTest() {
        val billOfLading = "data:application/pdf;base64,BillOfLading"
        val commercialInvoice = "data:application/pdf;base64,CommercialInvoice"
        val packingList = "data:application/pdf;base64,PackingList"
        val tradelaneData =
            SecureData(TestTradelane("Hauwert", TestTransporter("Erik", 1)))

        val merkletree = RootHash.build(
            listOf(
                SecureData(billOfLading),
                SecureData(commercialInvoice),
                SecureData(packingList),
                SecureData(tradelaneData)
            )
        )

        val merkletree2 = RootHash.build(
            listOf(
                SecureData(billOfLading),
                SecureData(commercialInvoice),
                SecureData(packingList),
                SecureData(tradelaneData)
            )
        )

        assertNotEquals(
            Hash(merkletree),
            Hash(merkletree2),
            "Hash not different despite of different nonce"
        )
    }

    @Test
    fun onlyHashesTest() {
        val billOfLading = SecureData("data:application/pdf;base64,BillOfLading")
        val commercialInvoice =
            SecureData("data:application/pdf;base64,CommercialInvoice")
        val packingList = SecureData("data:application/pdf;base64,PackingList")
        val tradelaneData =
            SecureData(TestTradelane("Hauwert", TestTransporter("Erik", 1)))

        val dataList = listOf(
            billOfLading,
            commercialInvoice,
            packingList,
            tradelaneData
        )

        val merkleTree = RootHash.build(dataList)

        val merkleTreeWithHash = RootHash.build(
            listOf(
                Hash(billOfLading),
                Hash(commercialInvoice),
                Hash(packingList),
                Hash(tradelaneData)
            )
        )

        assertEquals(
            Hash(merkleTree),
            Hash(merkleTreeWithHash)
        )

        assertEquals(0, merkleTreeWithHash.findData().size)

        assertEquals(dataList.size, merkleTree.findData().size)
    }

    @Test
    fun differentInputSequenceTest() {
        val billOfLading = SecureData("data:application/pdf;base64,BillOfLading")
        val commercialInvoice =
            SecureData("data:application/pdf;base64,CommercialInvoice")
        val packingList = SecureData("data:application/pdf;base64,PackingList")
        val tradelaneData =
            SecureData(TestTradelane("Hauwert", TestTransporter("Erik", 1)))

        val merkleTree = RootHash.build(
            listOf(
                billOfLading,
                commercialInvoice,
                packingList,
                tradelaneData
            )
        )

        val merkleTree2 = RootHash.build(
            listOf(
                tradelaneData,
                commercialInvoice,
                packingList,
                billOfLading
            )
        )


        assertEquals(
            Base64.toBase64String(merkleTree.sha3()),
            Base64.toBase64String(merkleTree2.sha3()),
            "Incorrect hash found, comparator not working?"
        )
    }
}
