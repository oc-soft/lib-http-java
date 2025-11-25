package net.oc_soft.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;



public class FieldMapTest1 {

    @Test
    void readFieldValue1() throws IOException {
        String fieldValueStr = "200\r\n\r\n";
        var srcBytes = fieldValueStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var fieldValue = FieldMap.readFieldValue(inputStream);
            assert fieldValue != null;
            var strValue = new String(fieldValue, StandardCharsets.UTF_8);
            assert "200".equals(strValue);

        }
         
    }

    @Test
    void readFieldValue2() throws IOException {
        String fieldValueStr = "200\r\n ABC\r\n\r\n";
        var srcBytes = fieldValueStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var fieldValue = FieldMap.readFieldValue(inputStream);
            assert fieldValue != null;
            var strValue = new String(fieldValue, StandardCharsets.UTF_8);
            assert "200\r\n ABC".equals(strValue);
        }
         
    }

    @Test
    void readFieldValue3() throws IOException {
        String fieldValueStr = "200\r\n ABC\r\n EFG\r\n\r\n";
        var srcBytes = fieldValueStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var fieldValue = FieldMap.readFieldValue(inputStream);
            assert fieldValue != null;
            var strValue = new String(fieldValue, StandardCharsets.UTF_8);
            assert "200\r\n ABC\r\n EFG".equals(strValue);
        }
         
    }

    @Test
    void readFieldValue4() throws IOException {
        String fieldValueStr = " \"ABC\"\r\n\r\n";
        var srcBytes = fieldValueStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var fieldValue = FieldMap.readFieldValue(inputStream);
            assert fieldValue != null;
            var strValue = new String(fieldValue, StandardCharsets.UTF_8);
            assert " \"ABC\"".equals(strValue);
        }
    }

    @Test
    void readFieldValue5() throws IOException {
        String fieldValueStr = " \"ABC\" \r\n A\r\n\r\n";
        var srcBytes = fieldValueStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var fieldValue = FieldMap.readFieldValue(inputStream);
            assert fieldValue != null;
            var strValue = new String(fieldValue, StandardCharsets.UTF_8);
            assert " \"ABC\" \r\n A".equals(strValue);
        }
    }


}

// vi: se ts=4 sw=4 et:
