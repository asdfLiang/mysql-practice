package org.example.support;

import java.util.UUID;

/**
 * @since 2024/12/12 14:35
 * @author by liangzj9624
 */
public class UUIDUtil {
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
