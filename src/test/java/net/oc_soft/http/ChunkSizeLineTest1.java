package net.oc_soft.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ChunkSizeLineTest1 {



    @Test
    void readChunkSizeLine1() throws IOException {
        var sizeStr = "a1f";
        var chunkSizeLineForm = 
            """
            %s \r
            \r
            """;
        var chunkSizeLineStr = String.format(chunkSizeLineForm, sizeStr);
        var srcBytes = chunkSizeLineStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var chunkSizeLineSize = ChunkSizeLine.read(inputStream);
            var chunkSizeLine = chunkSizeLineSize.chunkSizeLine();
            var size = Integer.parseInt(sizeStr, 16);
            
            assert chunkSizeLine.getChunkSize() == size :
                String.format("expected %d(%s) but %d",
                    size, sizeStr,
                    chunkSizeLine.getChunkSize());
        }
    } 

    @Test
    void readChunkSizeLine2() throws IOException {
        var sizeStr = "B02f";
        var chunkSizeLineForm = 
            """
            %s\r
            \r
            """;
        var chunkSizeLineStr = String.format(chunkSizeLineForm, sizeStr);
        var srcBytes = chunkSizeLineStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var chunkSizeLineSize = ChunkSizeLine.read(inputStream);
            var chunkSizeLine = chunkSizeLineSize.chunkSizeLine();
            var size = Integer.parseInt(sizeStr, 16);
            
            assert chunkSizeLine.getChunkSize() == size :
                String.format("expected %d(%s) but %d",
                    size, sizeStr,
                    chunkSizeLine.getChunkSize());
        }
    } 

    @Test
    void readChunkSizeLine3() throws IOException {
        var sizeStr = "B02f";
        var extensionName = "abc~";
        var extensionValue = "def";
        var chunkSizeLineForm = 
            """
            %s ; %s = %s\r
            \r
            """;
        var chunkSizeLineStr = String.format(
            chunkSizeLineForm, sizeStr, extensionName, extensionValue);
        var srcBytes = chunkSizeLineStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var chunkSizeLineSize = ChunkSizeLine.read(inputStream);
            var chunkSizeLine = chunkSizeLineSize.chunkSizeLine();
            assert chunkSizeLine.getExtensions().size()
                == 1 :
                String.format("extension size exptected %d but %d",
                    1, chunkSizeLine.getExtensions().size());
            assert chunkSizeLine.getExtension(
                extensionName).get(0).equals(extensionValue) :
                String.format("extension value expected %s but %s",
                    extensionValue,
                    chunkSizeLine.getExtension(
                        extensionName).get(0));
            
        }
    } 

    @Test
    void readChunkSizeLine4() throws IOException {
        var sizeStr = "B02f";
        var extensionName = "a";
        var extensionValue0 = "d";
        var extensionValue1 = String.format("\"%s\"", extensionValue0);
        var chunkSizeLineForm = 
            """
            %s ; %s = %s\r
            \r
            """;
        var chunkSizeLineStr = String.format(
            chunkSizeLineForm, sizeStr, extensionName, extensionValue1);
        var srcBytes = chunkSizeLineStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var chunkSizeLineSize = ChunkSizeLine.read(inputStream);
            var chunkSizeLine = chunkSizeLineSize.chunkSizeLine();
            assert chunkSizeLine.getExtensions().size() == 1 :
                String.format("extension size exptected %d but %d",
                    1, chunkSizeLine.getExtensions().size());
            assert chunkSizeLine.getExtension(
                extensionName).get(0).equals(extensionValue0) :
                String.format("extension value expected %s but %s",
                    extensionValue0,
                    chunkSizeLine.getExtension(extensionName));
            
        }
    } 

    @Test
    void readChunkSizeLine5() throws IOException {
        var sizeStr = "B02f";
        var nameValues = new Object[][] {
            {
                "abc", "123", false
            },
            {
                "abe", "456", false
            },
            {
                "efg", "abc  efg hji", true
            }
        };

        var extItems = Arrays.stream(nameValues).map((elem) -> {
            var strItems = new String[2];
            strItems[0] = String.format(" ; %s", elem[0]);
            if ((Boolean)elem[2]) {
                strItems[1] = String.format("\"%s\"", elem[1]); 
            } else {
                strItems[1] = (String)elem[1];
            }
            return String.join(" = ", strItems);
        }).toList();
        
        var extStr = String.join(" ", extItems);
         
        var chunkSizeLineForm = 
            """
            %s %s\r
            \r
            """;
        var chunkSizeLineStr = String.format(
            chunkSizeLineForm, sizeStr, extStr);
        var srcBytes = chunkSizeLineStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var chunkSizeLineSize = ChunkSizeLine.read(inputStream);
            var chunkSizeLine = chunkSizeLineSize.chunkSizeLine();
            assert chunkSizeLine.getExtensions().size() == nameValues.length:
                String.format("extension size exptected %d but %d",
                    nameValues.length, chunkSizeLine.getExtensions().size());

            for (var nameValue : nameValues) {
                var extensions = chunkSizeLine.getExtension(
                    nameValue[0].toString());
                assert extensions.get(0).equals(nameValue[1]) :
                    String.format("exptect extension of %s is %s but %s",
                        nameValue[0], nameValue[1],
                        extensions.get(0));
            }
        }
    } 
    @Test
    void readChunkSizeLine6() throws IOException {
        var sizeStr = "B02f";
        var nameValues = new Object[][] {
            {
                "abc", "123", false
            },
            {
                "abe", "456", false
            },
            {
                "efg", "abc  efg hji", true
            }
        };

        var extItems = Arrays.stream(nameValues).map((elem) -> {
            var strItems = new String[2];
            strItems[0] = String.format(";%s", elem[0]);
            if ((Boolean)elem[2]) {
                strItems[1] = String.format("\"%s\"", elem[1]); 
            } else {
                strItems[1] = (String)elem[1];
            }
            return String.join("=", strItems);
        }).toList();
        
        var extStr = String.join("", extItems);
         
        var chunkSizeLineForm = 
            """
            %s%s\r
            \r
            """;
        var chunkSizeLineStr = String.format(
            chunkSizeLineForm, sizeStr, extStr);
        var srcBytes = chunkSizeLineStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var chunkSizeLineSize = ChunkSizeLine.read(inputStream);
            var chunkSizeLine = chunkSizeLineSize.chunkSizeLine();
            assert chunkSizeLine.getExtensions().size() == nameValues.length:
                String.format("extension size exptected %d but %d",
                    nameValues.length, chunkSizeLine.getExtensions().size());

            for (var nameValue : nameValues) {
                var extensions = chunkSizeLine.getExtension(
                    nameValue[0].toString());
                assert extensions.get(0).equals(nameValue[1]) :
                    String.format("exptect extension of %s is %s but %s",
                        nameValue[0], nameValue[1],
                        extensions.get(0));
            }
        }
    } 

    @Test
    void readChunkSizeLine7() throws IOException {
        var sizeStr = "B02f";
        var nameValues = new Object[][] {
            {
                "abc", "123 456", true 
            },
            {
                "abe", "456 abc", true 
            },
            {
                "efg", "abc  efg hji", true
            }
        };

        var extItems = Arrays.stream(nameValues).map((elem) -> {
            var strItems = new String[2];
            strItems[0] = String.format(" ; %s", elem[0]);
            if ((Boolean)elem[2]) {
                strItems[1] = String.format("\"%s\"", elem[1]); 
            } else {
                strItems[1] = (String)elem[1];
            }
            return String.join(" = ", strItems);
        }).toList();
        
        var extStr = String.join(" ", extItems);
         
        var chunkSizeLineForm = 
            """
            %s %s\r
            \r
            """;
        var chunkSizeLineStr = String.format(
            chunkSizeLineForm, sizeStr, extStr);
        var srcBytes = chunkSizeLineStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var chunkSizeLineSize = ChunkSizeLine.read(inputStream);
            var chunkSizeLine = chunkSizeLineSize.chunkSizeLine();
            assert chunkSizeLine.getExtensions().size() == nameValues.length:
                String.format("extension size exptected %d but %d",
                    nameValues.length, chunkSizeLine.getExtensions().size());

            for (var nameValue : nameValues) {
                var extensions = chunkSizeLine.getExtension(
                    nameValue[0].toString());
                assert extensions.get(0).equals(nameValue[1]) :
                    String.format("exptect extension of %s is %s but %s",
                        nameValue[0], nameValue[1],
                        extensions.get(0));
            }
        }
    } 
    @Test
    void readChunkSizeLine8() throws IOException {
        var sizeStr = "B02f";
        var nameValues = new Object[][] {
            {
                "abc", "123 1sffa", true 
            },
            {
                "abe", "456\t123", true
            },
            {
                "efg", "abc  efg hji", true
            }
        };

        var extItems = Arrays.stream(nameValues).map((elem) -> {
            var strItems = new String[2];
            strItems[0] = String.format(";%s", elem[0]);
            if ((Boolean)elem[2]) {
                strItems[1] = String.format("\"%s\"", elem[1]); 
            } else {
                strItems[1] = (String)elem[1];
            }
            return String.join("=", strItems);
        }).toList();
        
        var extStr = String.join("", extItems);
         
        var chunkSizeLineForm = 
            """
            %s%s\r
            \r
            """;
        var chunkSizeLineStr = String.format(
            chunkSizeLineForm, sizeStr, extStr);
        var srcBytes = chunkSizeLineStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var chunkSizeLineSize = ChunkSizeLine.read(inputStream);
            var chunkSizeLine = chunkSizeLineSize.chunkSizeLine();
            assert chunkSizeLine.getExtensions().size() == nameValues.length:
                String.format("extension size exptected %d but %d",
                    nameValues.length, chunkSizeLine.getExtensions().size());

            for (var nameValue : nameValues) {
                var extensions = chunkSizeLine.getExtension(
                    nameValue[0].toString());
                assert extensions.get(0).equals(nameValue[1]) :
                    String.format("exptect extension of %s is %s but %s",
                        nameValue[0], nameValue[1],
                        extensions.get(0));
            }
        }
    }

    @Test
    void readChunkSizeLine9() throws IOException {
        var sizeStr = "1";
        var chunkSizeLineForm = 
            """
            %s\r
            """;
        var chunkSizeLineStr = String.format(chunkSizeLineForm, sizeStr);
        var srcBytes = chunkSizeLineStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var chunkSizeLineSize = ChunkSizeLine.read(inputStream);
            var chunkSizeLine = chunkSizeLineSize.chunkSizeLine();
            var size = Integer.parseInt(sizeStr, 16);
            
            assert chunkSizeLine.getChunkSize() == size :
                String.format("expected %d(%s) but %d",
                    size, sizeStr,
                    chunkSizeLine.getChunkSize());
        }
    } 

}
// vi: se ts=4 sw=4 et:
