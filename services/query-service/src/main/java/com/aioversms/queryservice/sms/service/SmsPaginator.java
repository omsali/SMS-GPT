package com.aioversms.queryservice.sms.service;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class SmsPaginator {

    private static final int SMS_LIMIT = 160;

    public List<String> paginate(String text) {
        if (text.length() <= SMS_LIMIT) {
            return List.of(text);
        }

        List<String> pages = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentChunk = new StringBuilder();

        for (String word : words) {
            // Check if adding this word exceeds the limit (leaving room for suffix " [xx/xx]")
            if (currentChunk.length() + word.length() + 1 > (SMS_LIMIT - 8)) {
                pages.add(currentChunk.toString());
                currentChunk = new StringBuilder();
            }
            if (!currentChunk.isEmpty()) {
                currentChunk.append(" ");
            }
            currentChunk.append(word);
        }
        
        if (!currentChunk.isEmpty()) {
            pages.add(currentChunk.toString());
        }

        // Add suffixes [1/3], [2/3]
        List<String> finalPages = new ArrayList<>();
        int total = pages.size();
        for (int i = 0; i < total; i++) {
            String suffix = String.format(" [%d/%d]", i + 1, total);
            // If it's the last page, we might imply "Reply MORE" in the future
            finalPages.add(pages.get(i) + suffix);
        }

        return finalPages;
    }
}