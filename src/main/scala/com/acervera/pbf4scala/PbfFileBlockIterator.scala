package com.acervera.pbf4scala

import java.io._

import org.openstreetmap.osmosis.osmbinary.fileformat.{Blob, BlobHeader}

object PbfFileBlockIterator {

  /**
    * Create a new PbfFileBlockIterator iterator.
    *
    * @param pbfInputStream Opened InputStream that contains the pbf
    * @return
    */
  def apply(pbfInputStream: InputStream) = new PbfFileBlockIterator(pbfInputStream)

}

/**
  * Iterator over a OSM file in pbf format.
  * Each item is a tuple of BlobHeader and Blob
  *
  * @param pbfInputStream Input stream that will be used to read the fileblock
  * @author angelcervera
  */
class PbfFileBlockIterator(pbfInputStream: InputStream) extends Iterator[(BlobHeader, Blob)] {

  // Read the input stream using DataInputStream to access easily to Int and raw fields.
  val pbfStream = new DataInputStream(pbfInputStream)

  // Store the next block length. None if there are not more to read.
  var nextBlockLength: Option[Int] = None

  // Read the length of the next block
  readNextBlockLength

  override def hasNext: Boolean = nextBlockLength.isDefined

  override def next(): (BlobHeader, Blob) = {

    // Reading header.
    val bufferBlobHeader = new Array[Byte](nextBlockLength.get)
    pbfStream.readFully(bufferBlobHeader)

    // Parsing pbf header.
    val blobHeader = BlobHeader parseFrom bufferBlobHeader

    // Read the next block
    val bufferBlob = new Array[Byte](blobHeader.datasize)
    pbfStream.readFully(bufferBlob)
    val blob = Blob parseFrom bufferBlob

    // Move to the next pair.
    readNextBlockLength

    (blobHeader, blob)

  }

  /**
    * Read the next osm pbf block
    */
  private def readNextBlockLength() = {

    try {
      nextBlockLength = Some(pbfStream.readInt)
    } catch {
      case e: EOFException => nextBlockLength = None
    }

  }

}
