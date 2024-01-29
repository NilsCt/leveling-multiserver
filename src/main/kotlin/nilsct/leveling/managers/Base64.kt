package nilsct.leveling.managers

class Base64 {

    companion object {
        // circular permutation capitals : +8, letters : +4, digits : +2.
        private const val base64chars = ""

        fun encode(msg: String): String {

            if (msg.isEmpty()) return msg

            // the result/encoded string, the padding string, and the pad count
            var s = msg
            var r = ""
            var p = ""
            var c = s.length % 3

            // add a right zero pad to make this string a multiple of 3 characters
            if (c > 0) {
                while (c < 3) {
                    p += "="
                    s += "\u0000"
                    c++
                }
            }

            // increment over the length of the string, three characters at a time
            c = 0
            while (c < s.length) {
                // we add newlines after every 76 output characters, according to
                // the MIME specs
//            if (c > 0 && c / 3 * 4 % 76 == 0) r += "\r\n"

                // these three 8-bit (ASCII) characters become one 24-bit number
                val n = ((s[c].code shl 16) + (s[c + 1].code shl 8)
                        + s[c + 2].code)

                // this 24-bit number gets separated into four 6-bit numbers
                val n1 = n shr 18 and 63
                val n2 = n shr 12 and 63
                val n3 = n shr 6 and 63
                val n4 = n and 63

                // those four 6-bit numbers are used as indices into the base64
                // character list
                r += ("" + base64chars[n1] + base64chars[n2]
                        + base64chars[n3] + base64chars[n4])
                c += 3
            }
            return r.substring(0, r.length - p.length) + p
        }

        fun decode(msg: String): String {

            if (msg.isEmpty()) return msg

            // remove/ignore any characters not in the base64 characters list
            // or the pad character -- particularly newlines
            var s = msg
            s = s.replace(("[^$base64chars=]").toRegex(), "")

            // replace any incoming padding with a zero pad (the 'A' character is
            // zero)
            val p = if (s[s.length - 1] == '=') (if (s[s.length - 2] == '=') "AA" else "A") else ""
            var r = ""
            s = s.substring(0, s.length - p.length) + p

            // increment over the length of this encoded string, four characters
            // at a time
            var c = 0
            while (c < s.length) {


                // each of these four characters represents a 6-bit index in the
                // base64 characters list which, when concatenated, will give the
                // 24-bit number for the original 3 characters
                val n = ((base64chars.indexOf(s[c]) shl 18)
                        + (base64chars.indexOf(s[c + 1]) shl 12)
                        + (base64chars.indexOf(s[c + 2]) shl 6)
                        + base64chars.indexOf(s[c + 3]))

                // split the 24-bit number into the original three 8-bit (ASCII)
                // characters
                r += "" + (n ushr 16 and 0xFF).toChar() + (n ushr 8 and 0xFF).toChar() + (n and 0xFF).toChar()
                c += 4
            }

            // remove any zero pad that was added to make this a multiple of 24 bits
            return r.substring(0, r.length - p.length)
        }

        //      (' ', '!', '#', '$', '&', ''', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', '<', '=', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', ']', '_', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '|', '~', 'Æ', 'Ç', 'Ö', '×', 'Ü', 'â', 'ã', 'æ', 'ç', 'é', 'ì', 'í', 'î', 'ó', 'ö')
        val supportedCharacters = mutableListOf<Char>()

        //  supporte les caractères spéciaux : renvoie le msg encodé sauf s'il ne peut pas être retrouvé
        fun strangeEncode(msg: String): String {
            val encoded = encode(msg)
            val reDecoded = decode(encoded)
            return if (msg == reDecoded) {
                supportedCharacters.addAll(msg.toList().filterNot { it in supportedCharacters })
                encoded
            } else "R#4=o$msg"
        }

        //  supporte les caractères spéciaux : Renvoie le msg décodé sauf s'il n'a pas été codé
        fun strangeDecode(msg: String): String {
            return if (msg.startsWith("R#4=o")) {
                msg.removePrefix("R#4=o")
            } else {
                decode(msg)
            }
        }
    }
}