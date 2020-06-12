package com.luckyun.config.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

public class PyRelationUtils {

	public static void properties2Yaml(String s,String propertiesData) {
		JsonParser parser = null;
		JavaPropsFactory factory = new JavaPropsFactory();
		YAMLGenerator generator = null;
		try {
			parser = factory.createParser(propertiesData);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		try {
			YAMLFactory yamlFactory = new YAMLFactory();
			generator = yamlFactory
					.createGenerator(new OutputStreamWriter(new FileOutputStream(s), Charset.forName("UTF-8")));

			JsonToken token = parser.nextToken();

			while (token != null) {
				if (JsonToken.START_OBJECT.equals(token)) {
					generator.writeStartObject();
				} else if (JsonToken.FIELD_NAME.equals(token)) {
					generator.writeFieldName(parser.getCurrentName());
				} else if (JsonToken.VALUE_STRING.equals(token)) {
					generator.writeString(parser.getText());
				} else if (JsonToken.END_OBJECT.equals(token)) {
					generator.writeEndObject();
				}
				token = parser.nextToken();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (parser != null) {
					parser.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (generator != null) {
				try {
					generator.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static List<String> yml2Properties(String s) {
        final String DOT = ".";
        List<String> lines = new LinkedList<>();
        try {
            YAMLFactory yamlFactory = new YAMLFactory();
            YAMLParser parser = yamlFactory.createParser(s);
            String key = "";
            String value ;
            JsonToken token = parser.nextToken();
            while (token != null) {
                if (JsonToken.START_OBJECT.equals(token)) {
                    // "{" 表示字符串开头，不做解析
                } else if (JsonToken.FIELD_NAME.equals(token)) {
                    if (key.length() > 0) {
                        key = key + DOT;
                    }
                    key = key + parser.getCurrentName();
                    token = parser.nextToken();
                    if (JsonToken.START_OBJECT.equals(token)) {
                        continue;
                    }
                    value = parser.getText();
                    lines.add(key + "=" + value);
                    int dotOffset = key.lastIndexOf(DOT);
                    if (dotOffset > 0) {
                        key = key.substring(0, dotOffset);
                    }
                } else if (JsonToken.END_OBJECT.equals(token)) {
                    int dotOffset = key.lastIndexOf(DOT);
                    if (dotOffset > 0) {
                        key = key.substring(0, dotOffset);
                    } else {
                        key = "";
                        lines.add("");
                    }
                }
                token = parser.nextToken();
            }
            parser.close();
        return lines;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
