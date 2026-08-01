package com.cchangy.concurrent.util;

import org.openjdk.jol.info.ClassLayout;

/**
 * JOL 解析工具
 *
 * @author cchangy
 * @date 2026/07/26 15:12
 */
public class JOLParser {

    /**
     * 解析对象的 Mark Word，返回结构化信息
     */
    public static String parse(Object obj) {
        // 获取 JOL 输出
        String layout = ClassLayout.parseInstance(obj).toPrintable();
        String markLine = findMarkLine(layout);

        if (markLine == null) {
            return "错误: 无法找到 Mark Word";
        }

        // 提取十六进制值
        String hexValue = extractHex(markLine);
        if (hexValue.isEmpty()) {
            return "错误: 无法解析 Mark Word 值";
        }

        // 提取描述信息 (biasable; age: 0)
        String description = extractDescription(markLine);

        // 解析 Mark Word
        long mark = Long.parseUnsignedLong(hexValue.substring(2), 16);

        return buildResult(markLine, mark, description);
    }

    /**
     * 构建解析结果
     */
    private static String buildResult(String hexValue, long mark, String description) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n======================Mark Work解析======================");
        // 1. 原始值
        sb.append("\nMark Word: ").append(hexValue).append("\n");

        // 2. 二进制（64位，每8位一组）
        sb.append("二进制:   ").append(formatBinary(mark)).append("\n");

        // 3. 锁状态
        String lockState = getLockState(mark, description);
        sb.append("锁状态:   ").append(lockState);

        // 如果是偏向锁，显示线程ID
        if (lockState.contains("偏向锁")) {
            long threadId = mark >> 6;
            if (threadId != 0) {
                sb.append("\n线程ID:   ").append(threadId);
            } else {
                sb.append("\n线程ID:   无 (匿名偏向)");
            }
        }

        // 如果是无锁且有hashCode，显示hashCode
        if (lockState.equals("无锁 (不可偏向)")) {
            long hashCode = (mark >> 8) & 0xFFFFFFFFL;
            if (hashCode != 0) {
                sb.append("\nHashCode: 0x").append(Long.toHexString(hashCode));
            }
        }

        sb.append("\n========================================================\n");
        return sb.toString();
    }

    /**
     * 判断锁状态
     */
    private static String getLockState(long mark, String description) {
        long lockBits = mark & 0b111;
        long biasedFlag = (mark >> 2) & 0b1;  // 第3位（从0开始）

        // 1. 优先检查描述中的 non-biasable
        if (description.contains("non-biasable")) {
            // 不可偏向，根据 lockBits 判断具体状态
            switch ((int) lockBits) {
                case 0b001:
                    return "无锁 (不可偏向)";
                case 0b000:
                    return "轻量级锁 (不可偏向)";
                case 0b010:
                    return "重量级锁 (不可偏向)";
                default:
                    return "未知 (不可偏向)";
            }
        }

        // 2. 检查描述中的 biasable
        if (description.contains("biasable")) {
            // 可偏向
            if (lockBits == 0b101) {
                long threadId = mark >> 6;
                return threadId == 0 ? "偏向锁 (匿名)" : "偏向锁";
            }
            if (lockBits == 0b001) {
                return "无锁 (可偏向)";
            }
        }

        // 3. 如果描述中没有明确信息，根据位判断
        switch ((int) lockBits) {
            case 0b001:
                // 无锁：检查偏向标志位
                if (biasedFlag == 1) {
                    return "无锁 (可偏向)";
                } else {
                    return "无锁 (不可偏向)";
                }
            case 0b101:
                return "偏向锁";
            case 0b000:
                return "轻量级锁";
            case 0b010:
                return "重量级锁";
            case 0b011:
                return "GC标记";
            default:
                return "未知状态";
        }
    }

    /**
     * 格式化二进制（每8位一组）
     */
    private static String formatBinary(long value) {
        String binary = String.format("%64s", Long.toBinaryString(value))
                .replace(' ', '0');
        // 每8位加一个空格
        return binary.replaceAll("(.{8})", "$1 ").trim();
    }

    /**
     * 查找 Mark Word 行
     */
    private static String findMarkLine(String layout) {
        for (String line : layout.split("\n")) {
            if (line.contains("object header: mark")) {
                return line;
            }
        }
        return null;
    }

    /**
     * 提取十六进制值
     */
    private static String extractHex(String line) {
        for (String part : line.split("\\s+")) {
            if (part.startsWith("0x")) {
                return part;
            }
        }
        return "";
    }

    /**
     * 提取描述信息（括号内的内容）
     */
    private static String extractDescription(String line) {
        int start = line.indexOf('(');
        int end = line.lastIndexOf(')');
        if (start != -1 && end != -1 && end > start) {
            return line.substring(start + 1, end);
        }
        return "";
    }
}
