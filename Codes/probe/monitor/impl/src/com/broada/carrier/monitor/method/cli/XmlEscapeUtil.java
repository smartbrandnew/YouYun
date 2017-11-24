package com.broada.carrier.monitor.method.cli;

public class XmlEscapeUtil {
  public static final int HIGHEST_SPECIAL = '>';

  public static char[][] specialCharactersRepresentation = new char[HIGHEST_SPECIAL + 1][];
  static {
    specialCharactersRepresentation['&'] = "&amp;".toCharArray();
    specialCharactersRepresentation['<'] = "&lt;".toCharArray();
    specialCharactersRepresentation['>'] = "&gt;".toCharArray();
    specialCharactersRepresentation['"'] = "&#034;".toCharArray();
    specialCharactersRepresentation['\''] = "&#039;".toCharArray();
    specialCharactersRepresentation['\n'] = "<br>".toCharArray();
    //add by huangjb 2008/01/23 添加空格转换
    specialCharactersRepresentation[' '] = "&nbsp;".toCharArray();
  }

  /**
   * Performs the following substring replacements (to facilitate output to XML/HTML pages):
   *  & -> &amp; < -> &lt; > -> &gt; " -> &#034; ' -> &#039;
   * 
   * See also OutSupport.writeEscapedXml().
   */
  public static String escapeXml(String buffer) {
    int start = 0;
    int length = buffer.length();
    char[] arrayBuffer = buffer.toCharArray();
    StringBuffer escapedBuffer = null;

    for (int i = 0; i < length; i++) {
      char c = arrayBuffer[i];
      if (c <= HIGHEST_SPECIAL) {
        char[] escaped = specialCharactersRepresentation[c];
        if (escaped != null) {
          // create StringBuffer to hold escaped xml string
          if (start == 0) {
            escapedBuffer = new StringBuffer(length + 5);
          }
          // add unescaped portion
          if (start < i) {
            escapedBuffer.append(arrayBuffer, start, i - start);
          }
          start = i + 1;
          // add escaped xml
          escapedBuffer.append(escaped);
        }
      }
    }
    // no xml escaping was necessary
    if (start == 0) {
      return buffer;
    }
    // add rest of unescaped portion
    if (start < length) {
      escapedBuffer.append(arrayBuffer, start, length - start);
    }
    return escapedBuffer.toString();
  }
}
