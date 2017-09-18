/**
 * Copyright © 2007 Chu Yeow Cheah
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Copied verbatim from http://dzone.com/snippets/java-inflections, used 
 * and licensed with express permission from the author Chu Yeow Cheah.
 */

package org.jsonschema2pojo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transforms words (from singular to plural, from camelCase to under_score,
 * etc.). I got bored of doing Real Work...
 * 
 * @author chuyeow
 */
public class Inflector {

    private final List<RuleAndReplacement> plurals;
    private final List<RuleAndReplacement> singulars;
    private final List<String> uncountables;

    private static Inflector instance  = createDefaultBuilder().build();

    private Inflector(Builder builder) {
        plurals = Collections.unmodifiableList(builder.plurals);
        singulars = Collections.unmodifiableList(builder.singulars);
        uncountables = Collections.unmodifiableList(builder.uncountables);
    }

    public static Inflector.Builder createDefaultBuilder()
    {
        Builder builder = builder();

        builder.plural("$", "s")
            .plural("s$", "s")
            .plural("(ax|test)is$", "$1es")
            .plural("(octop|vir)us$", "$1i")
            .plural("(alias|status)$", "$1es")
            .plural("(bu)s$", "$1es")
            .plural("(buffal|tomat)o$", "$1oes")
            .plural("([ti])um$", "$1a")
            .plural("sis$", "ses")
            .plural("(?:([^f])fe|([lr])f)$", "$1$2ves")
            .plural("(database|hive)$", "$1s")
            .plural("([^aeiouy]|qu)y$", "$1ies")
            .plural("([^aeiouy]|qu)ies$", "$1y")
            .plural("(x|ch|ss|sh)$", "$1es")
            .plural("(matr|vert|ind)ix|ex$", "$1ices")
            .plural("([m|l])ouse$", "$1ice")
            .plural("(ox)$", "$1en")
            .plural("man$", "men")
            .plural("(quiz)$", "$1zes")
            .plural("specimen", "specimens");

        builder.singular("s$", "")
            .singular("(n)ews$", "$1ews")
            .singular("([ti])a$", "$1um")
            .singular("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "$1$2sis")
            .singular("(^analy)ses$", "$1sis")
            .singular("([^f])ves$", "$1fe")
            .singular("(database|hive)s$", "$1")
            .singular("(tive)s$", "$1")
            .singular("([lr])ves$", "$1f")
            .singular("([^aeiouy]|qu)ies$", "$1y")
            .singular("(s)eries$", "$1eries")
            .singular("(m)ovies$", "$1ovie")
            .singular("(x|ch|ss|sh)es$", "$1")
            .singular("([m|l])ice$", "$1ouse")
            .singular("(bus)es$", "$1")
            .singular("(o)es$", "$1")
            .singular("(shoe)s$", "$1")
            .singular("(cris|ax|test)es$", "$1is")
            .singular("(tax)es$", "$1")
            .singular("([octop|vir])i$", "$1us")
            .singular("(alias|status)es$", "$1")
            .singular("^(ox)en", "$1")
            .singular("(vert|ind)ices$", "$1ex")
            .singular("(matr)ices$", "$1ix")
            .singular("(quiz)zes$", "$1")
            .singular("(ess)$", "$1")
            .singular("men$", "man")
            .singular("specimen", "specimen")
            .singular("status$", "status");

        builder.irregular("curve", "curves")
            .irregular("leaf", "leaves")
            .irregular("roof", "rooves")
            .irregular("person", "people")
            .irregular("child", "children")
            .irregular("sex", "sexes")
            .irregular("move", "moves");

        builder.uncountable(new String[] { "equipment", "information", "rice", "money", "species", "series", "fish", "sheep", "s" });

        return builder;
    }

    public static Inflector getInstance() {
        return instance;
    }

    public String pluralize(String word) {
        if (uncountables.contains(word.toLowerCase())) {
            return word;
        }
        return replaceWithFirstRule(word, plurals);
    }

    public String singularize(String word) {
        if (uncountables.contains(word.toLowerCase())) {
            return word;
        }
        return replaceWithFirstRule(word, singulars);
    }

    private static String replaceWithFirstRule(String word, List<RuleAndReplacement> ruleAndReplacements) {

        for (RuleAndReplacement rar : ruleAndReplacements) {
            String replacement = rar.getReplacement();

            // Return if we find a match.
            Matcher matcher = rar.getPattern().matcher(word);
            if (matcher.find()) {
                return matcher.replaceAll(replacement);
            }
        }
        return word;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    // Ugh, no open structs in Java (not-natively at least).
    private static class RuleAndReplacement {
        private final String replacement;
        private final Pattern pattern;

        public RuleAndReplacement(String rule, String replacement) {
            this.replacement = replacement;
            this.pattern = Pattern.compile(rule, Pattern.CASE_INSENSITIVE);
        }

        public String getReplacement() {
            return replacement;
        }

        public Pattern getPattern() {
            return pattern;
        }
    }

    public static class Builder
    {
        private List<RuleAndReplacement> plurals = new ArrayList<RuleAndReplacement>();
        private List<RuleAndReplacement> singulars = new ArrayList<RuleAndReplacement>();
        private List<String> uncountables = new ArrayList<String>();

        public Builder plural(String rule, String replacement) {
            plurals.add(0, new RuleAndReplacement(rule, replacement));
            return this;
        }

        public Builder singular(String rule, String replacement) {
            singulars.add(0, new RuleAndReplacement(rule, replacement));
            return this;
        }

        public Builder irregular(String singular, String plural) {
            plural(singular, plural);
            singular(plural, singular);
            return this;
        }

        public Builder uncountable(String... words) {
            for (String word : words) {
                uncountables.add(word);
            }
            return this;
        }

        public Inflector build()
        {
            return new Inflector(this);
        }
    }
}
