package org.iclassq.util.voice;

import java.util.*;
import java.util.stream.Collectors;

public class KeywordGenerator {
    public static String generateKeywords(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return nombre;
        }

        Set<String> keywords = new LinkedHashSet<>();
        String normalized = normalize(nombre);
        keywords.add(normalized);

        String[] words = tokenize(normalized);
        for (String word : words) {
            if (word.length() >= 2) {
                keywords.add(word);
                keywords.addAll(generatePhoneticVariations(word));
            }
        }

        keywords.addAll(processAcronyms(nombre));
        keywords.addAll(generateNGrams(words, 2));
        keywords.addAll(generateNGrams(words, 3));

        for (String word : words) {
            if (word.length() >= 4) {
                keywords.addAll(generateSyllableVariations(word));
            }
        }

        return keywords.stream()
                .filter(k -> k != null && !k.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.joining(","));
    }

    private static Set<String> generatePhoneticVariations(String word) {
        Set<String> variations = new LinkedHashSet<>();
        variations.add(word);

        if (word.contains("b")) {
            variations.add(word.replace("b", "v"));
        }

        if (word.contains("v")) {
            variations.add(word.replace("v", "b"));
        }

        String temp = word;
        temp = temp.replace("ce", "se");
        temp = temp.replace("ci", "si");
        if (!temp.equals(word)) {
            variations.add(temp);
        }

        if (word.contains("z")) {
            variations.add(word.replace("z", "s"));
        }

        if (word.contains("ll")) {
            variations.add(word.replace("ll", "y"));
        }

        if (word.contains("y")) {
            variations.add(word.replace("y", "ll"));
        }

        if (word.contains("h")) {
            variations.add(word.replace("h", ""));
        }

        if (word.contains("qu")) {
            variations.add(word.replace("qu", "k"));
            variations.add(word.replace("qu", "c"));
        }

        if (word.contains("ge") || word.contains("gi")) {
            String variant = word.replace("ge", "je").replace("gi", "ji");
            variations.add(variant);
        }

        if (word.startsWith("x")) {
            variations.add("s" + word.substring(1));
        }

        temp = word;
        temp = temp.replace("cc", "c");
        temp = temp.replace("nn", "n");
        if (!temp.equals(word)) {
            variations.add(temp);
        }

        return variations;
    }

    private static Set<String> processAcronyms(String text) {
        Set<String> variations = new LinkedHashSet<>();

        String[] words = text.split("\\s+");
        for (String word : words) {
            String cleaned = word.replaceAll("[^A-Za-z]", "");

            if (cleaned.length() >= 2 && cleaned.length() <= 5) {
                boolean isAcronym = cleaned.equals(cleaned.toUpperCase()) ||
                                    (cleaned.length() <= 4 && !cleaned.matches(".*[aeiou].*"));

                if (isAcronym) {
                    String lower = cleaned.toLowerCase();
                    variations.add(lower);
                    variations.add(spellOut(lower));
                    variations.add(String.join(" ", lower.split("")));
                }
            }
        }

        return variations;
    }

    private static String spellOut(String text) {
        StringBuilder spelled = new StringBuilder();

        for (char c : text.toLowerCase().toCharArray()) {
            if (spelled.length() > 0) {
                spelled.append(" ");
            }

            spelled.append(getLetterName(c));
        }

        return spelled.toString();
    }

    private static String getLetterName(char c) {
        switch (c) {
            case 'a': return "a";
            case 'b': return "be";
            case 'c': return "ce";
            case 'd': return "de";
            case 'e': return "e";
            case 'f': return "efe";
            case 'g': return "ge";
            case 'h': return "hache";
            case 'i': return "i";
            case 'j': return "jota";
            case 'k': return "ka";
            case 'l': return "ele";
            case 'm': return "eme";
            case 'n': return "ene";
            case 'o': return "o";
            case 'p': return "pe";
            case 'q': return "cu";
            case 'r': return "erre";
            case 's': return "ese";
            case 't': return "te";
            case 'u': return "u";
            case 'v': return "ve";
            case 'w': return "doble ve";
            case 'x': return "equis";
            case 'y': return "ye";
            case 'z': return "zeta";
            default: return String.valueOf(c);
        }
    }

    private static Set<String> generateNGrams(String[] words, int n) {
        Set<String> ngrams = new LinkedHashSet<>();

        if (words.length < n) {
            return ngrams;
        }

        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder ngram = new StringBuilder();

            for (int j = 0; j < n; j++) {
                if (words[i + j].length() >= 2) {
                    if (ngram.length() > 0) {
                        ngram.append(" ");
                    }

                    ngram.append(words[i + j]);
                }
            }

            if (ngram.length() > 0) {
                ngrams.add(ngram.toString());
            }
        }

        return ngrams;
    }

    private static Set<String> generateSyllableVariations(String word) {
        Set<String> variations = new LinkedHashSet<>();

        List<String> syllables = approximateSyllables(word);

        if (syllables.size() > 1) {
            variations.add(syllables.get(0));

            variations.add(syllables.get(syllables.size() - 1));

            if (syllables.size() >= 2) {
                variations.add(syllables.get(0) + syllables.get(1));
            }
        }

        return variations;
    }

    private static List<String> approximateSyllables(String word) {
        List<String> syllables = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            current.append(c);

            if (isVowel(c) && i + 1 < word.length() && !isVowel(word.charAt(i + 1))) {
                if (i + 2 < word.length() && isVowel(word.charAt(i + 2))) {
                    syllables.add(current.toString());
                    current = new StringBuilder();
                }
            }
        }

        if (current.length() > 0) {
            syllables.add(current.toString());
        }

        return syllables.isEmpty() ? Arrays.asList(word) : syllables;
    }

    private static boolean isVowel(char c) {
        return "aeiouáéíóú".indexOf(Character.toLowerCase(c)) >= 0;
    }

    private static String[] tokenize(String text) {
        return Arrays.stream(text.split("\\s+"))
                .filter(w -> w.length() >= 2)
                .filter(w -> !isStopWord(w))
                .toArray(String[]::new);
    }

    private static boolean isStopWord(String word) {
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "el", "la", "de", "del", "al", "un", "una", "y", "o", "en", "por", "para", "con"
        ));
        return stopWords.contains(word.toLowerCase());
    }

    private static String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("ñ", "n")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
