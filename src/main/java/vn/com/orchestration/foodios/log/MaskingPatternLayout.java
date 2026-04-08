package vn.com.orchestration.foodios.log;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class MaskingPatternLayout extends PatternLayout {

    private Pattern maskMultilinePattern;
    private Pattern hideMultilinePattern;

    private final List<String> maskPatterns = new ArrayList<>();
    private final List<String> hidePatterns = new ArrayList<>();

    public void addMaskPattern(String maskPattern) {
        maskPatterns.add(maskPattern);
        maskMultilinePattern = Pattern.compile(
                String.join("|", maskPatterns),
                Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
    }
    public void addHidePattern(String hidePattern) {
        hidePatterns.add(hidePattern);
        this.hideMultilinePattern = Pattern.compile(
                String.join("|", hidePatterns),
                Pattern.MULTILINE | Pattern.DOTALL
        );
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        String formatted = super.doLayout(event);
        return maskMessage(formatted);
    }

    private String maskMessage(String message) {
        if (message == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(message);

        if (maskMultilinePattern != null) {
            Matcher matcher = maskMultilinePattern.matcher(sb);
            while (matcher.find()) {
                IntStream.rangeClosed(1, matcher.groupCount()).forEach(group -> {
                    if (matcher.group(group) != null) {
                        IntStream.range(matcher.start(group), matcher.end(group))
                                .forEach(i -> sb.setCharAt(i, '*'));
                    }
                });
            }
        }
        if (hideMultilinePattern != null) {
            Matcher matcher = hideMultilinePattern.matcher(sb);
            Map<Integer, Integer> replaceIndex = new LinkedHashMap<>();
            while (matcher.find()) {
                IntStream.rangeClosed(1, matcher.groupCount()).forEach(group -> {
                    if (matcher.group(group) != null) {
                        replaceIndex.put(matcher.start(group), matcher.end(group));
                    }
                });
            }
            List<Map.Entry<Integer, Integer>> entries = new ArrayList<>(replaceIndex.entrySet());
            Collections.reverse(entries);
            for (Map.Entry<Integer, Integer> e : entries) {
                sb.replace(e.getKey(), e.getValue(), "DONT SHOW");
            }
        }

        return sb.toString();
    }
}