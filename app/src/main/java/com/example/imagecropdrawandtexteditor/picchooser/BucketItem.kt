
package com.example.imagecropdrawandtexteditor.picchooser

internal class BucketItem(var n: String, p: String?, taken: String?, val id: Int) : GridItem(
    n, p!!, taken!!, 0) {
    var images = 1
}