package com.nurullah.util;

import org.junit.jupiter.params.provider.Arguments;

import java.net.URISyntaxException;
import java.util.stream.Stream;

import static com.nurullah.util.RequestUtil.requestTo;

public class Parameter {
    public static Stream<Arguments> getParameters() throws URISyntaxException {
        return Stream.of(
                Arguments.of(requestTo("test"), 200, "New Header", "{\"name\":\"Nurullah\",\"age\":25}"),
                Arguments.of(requestTo("notfound"), 404, "", "")
        );

    }
}
