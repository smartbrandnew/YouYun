package com.broada.carrier.monitor.method.cli.session;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 协议相关的工具类
 * 
 * @author chenliang1
 * 
 */
public class ProtocolUtils {
  private static final Log logger = LogFactory.getLog(ProtocolUtils.class);
  private static boolean traceEnabled = logger.isTraceEnabled();
  protected static final byte[] specialPrefix = new byte[] { 0x0, 0x1B, 0x5B };
  private static FileOutputStream out;
  private static byte[] original = "\nOriginal:".getBytes();
  private static byte[] processed = "\nProcessed:".getBytes();

  /**
   * 去除特殊字符
   * 
   * @param buffer
   * @param len
   * @param prompt
   * @return
   */
  public static int removeSpecialCharacters(byte[] buffer, int len, String prompt) {
    if (traceEnabled && out == null) {
      try {
        out = new FileOutputStream("logs/telnet.log", false);
      } catch (FileNotFoundException e) {
        traceEnabled = false;
        try {
          out.close();
        } catch(IOException ex) {
          logger.debug("文件输出流关闭异常", ex);
        }
        out = null;
        logger.warn("logs/telnet.log文件未找到", e);
        e.printStackTrace();
      }
    }
    if (len <= 0)
      return len;
    if (traceEnabled && out != null) {
      try {
        out.write(original);
        out.write(buffer, 0, len);
      } catch (IOException e) {
        traceEnabled = false;
        try {
          out.close();
        } catch(IOException ex) {
          logger.debug("文件输出流关闭异常", ex);
        }
        out = null;
        logger.warn("文件输出流异常", e);
        e.printStackTrace();
      }
    }
    // 是否存在特殊前缀
    boolean hasSpecialPrefix = false;
    // 若存在特殊前缀
    if (startWith(buffer, len, specialPrefix)) {
      hasSpecialPrefix = true;
      len -= specialPrefix.length;
      System.arraycopy(buffer, specialPrefix.length, buffer, 0, len);
    }

    len = removeSpecialCharacters(buffer, len);

    // 若存在特殊前缀，则去掉从开头到第一个提示符为止的数据
    if (hasSpecialPrefix) {
      int index = searchForPrompt(buffer, len, prompt);
      if (index > 0) {
        len -= index;
        System.arraycopy(buffer, index, buffer, 0, len);
        hasSpecialPrefix = false;
      }
    }
    if (traceEnabled && out != null) {
      try {
        out.write(processed);
        out.write(buffer, 0, len);
      } catch (IOException e) {
        traceEnabled = false;
        try {
          out.close();
        } catch(IOException ex) {
          logger.debug("文件输出流关闭异常", ex);
        }
        out = null;
        logger.warn("文件输出流异常", e);
        e.printStackTrace();
      }
    }
    return len;
  }

  /**
   * 去除特殊字符
   * 
   * @param buffer
   * @param len
   * @return
   */
  public static int removeSpecialCharacters(byte[] buffer, int len) {
    // 去除控制字符
    for (int i = 0; i < len;) {
      if (buffer[i] == '\b') {
        if (i == 0) {
          System.arraycopy(buffer, 1, buffer, 0, len - 1);
          len--;
        } else {
          System.arraycopy(buffer, i + 1, buffer, i - 1, len - i - 1);
          len -= 2;
          i--;
        }
        continue;
      } else if (buffer[i] == '\u001B') { // ESC
        if (buffer[i + 1] == '[') {
          int j;
          for (j = i + 2; j < len; j++) {
            if (Character.isLetter(buffer[j])) {
              j++;
              break;
            }
          }
          System.arraycopy(buffer, j, buffer, i, len - j);
          len -= j - i;
        } else {
          System.arraycopy(buffer, i + 1, buffer, i, len - i - 1);
          len--;
        }
        continue;
      } else if (buffer[i] == '\r') {
        if (i + 1 >= len || buffer[i + 1] != '\n') {
          buffer[i] = '\n';
        }
      } else if (buffer[i] == '\0') {
        System.arraycopy(buffer, i + 1, buffer, i, len - i - 1);
        len--;
      }
      i++;
    }
    return len;
  }

  public static boolean startWith(byte[] target, int len, byte[] prefix) {
    if (len < prefix.length)
      return false;
    for (int i = 0; i < prefix.length; i++) {
      if (target[i] != prefix[i])
        return false;
    }
    return true;
  }

  /**
   * 查找提示符之后的内容的起始位置，如果不存在提示符则返回-1
   * 
   * @param target
   *          待查找的数组
   * @param len
   *          待查找的数据的长度
   * @param prompt
   *          提示符
   * @return
   */
  public static int searchForPrompt(byte[] target, int len, String prompt) {
    byte[] prompt_ = prompt.getBytes();
    boolean lineStart = false;
    outer: for (int i = 0; i < len - prompt_.length + 1; i++) {
      if (target[i] == '\n' || target[i] == '\r') {
        lineStart = true;
      } else if (lineStart && target[i] != ' ' && target[i] != '\0') {
        lineStart = false;
        for (int j = 0; j < prompt_.length; j++) {
          if (target[i + j] != prompt_[j])
            continue outer;
        }
        int result = i + prompt_.length;
        if (result + 1 < len && target[result + 1] == ' ')
          result++;
        return result;
      }
    }
    return -1;
  }

  /**
   * 从登陆信息中获取完整的命令行提示符。不完整的提示符可能会导致错误识别提示符从而导致无法正常解析结果。
   * 
   * @param loginMessage
   * @return
   */
  public static String getPromptFromLoginMessage(String loginMessage) {
    if (StringUtils.isBlank(loginMessage))
      return null;
    int pos = loginMessage.lastIndexOf('\n');
    return loginMessage.substring(pos + 1);
  }

  public static boolean containsIgnoreWhiteSpaces(String source, String target) {
    return containsIgnoreWhiteSpaces(source.toCharArray(), target.toCharArray());
  }
  
  public static boolean containsIgnoreWhiteSpaces(char[] source, char[] target) {
    int sourceCount = 0;
    for (int i = 0; i < source.length; i++) {
      if (!Character.isSpaceChar(source[i]))
        source[sourceCount++] = source[i];
    }
    int targetCount = 0;
    for (int i = 0; i < target.length; i++) {
      if (!Character.isSpaceChar(target[i]))
        target[targetCount++] = target[i];
    }
    return indexOf(source, 0, sourceCount, target, 0, targetCount, 0) >= 0;
  }

  /**
   * Code shared by String and StringBuffer to do searches. The source is the
   * character array being searched, and the target is the string being searched
   * for.
   * 
   * @param source
   *          the characters being searched.
   * @param sourceOffset
   *          offset of the source string.
   * @param sourceCount
   *          count of the source string.
   * @param target
   *          the characters being searched for.
   * @param targetOffset
   *          offset of the target string.
   * @param targetCount
   *          count of the target string.
   * @param fromIndex
   *          the index to begin searching from.
   */
  protected static int indexOf(char[] source, int sourceOffset, int sourceCount, char[] target, int targetOffset,
      int targetCount, int fromIndex) {
    if (fromIndex >= sourceCount) {
      return (targetCount == 0 ? sourceCount : -1);
    }
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    if (targetCount == 0) {
      return fromIndex;
    }

    char first = target[targetOffset];
    int max = sourceOffset + (sourceCount - targetCount);

    for (int i = sourceOffset + fromIndex; i <= max; i++) {
      /* Look for first character. */
      if (source[i] != first) {
        while (++i <= max && source[i] != first)
          ;
      }

      /* Found first character, now look at the rest of v2 */
      if (i <= max) {
        int j = i + 1;
        int end = j + targetCount - 1;
        for (int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++)
          ;

        if (j == end) {
          /* Found whole string. */
          return i - sourceOffset;
        }
      }
    }
    return -1;
  }
}
