/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Testing strategy
 *
 * guessFollowsGraph(tweets):
 *   tweets size: 0, 1, > 1
 *   mentions per tweet: 0, 1, > 1
 *   authors: one author with many tweets, many authors
 *   username case: same name in different case
 *   self-mention
 *
 * influencers(followsGraph):
 *   graph size: empty, one user, many users
 *   followers: 0, 1, > 1
 *   ordering: strictly different counts, tied counts
 *
 * The postcondition of guessFollowsGraph() is underdetermined, so these tests
 * only check that the required evidence (author @-mentions user) is present
 * in the result. They never check that the graph is exactly equal to something.
 */
public class SocialNetworkTest {

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");

    private static Tweet tweet(long id, String author, String text) {
        return new Tweet(id, author, text, d1);
    }

    private static Set<String> setOf(String... names) {
        return new HashSet<>(Arrays.asList(names));
    }

    // returns a lower-cased copy of the graph so case differences can't fail a test
    private static Map<String, Set<String>> lowerCased(Map<String, Set<String>> graph) {
        Map<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            Set<String> follows = new HashSet<>();
            for (String name : entry.getValue()) {
                follows.add(name.toLowerCase());
            }
            result.put(entry.getKey().toLowerCase(), follows);
        }
        return result;
    }

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // ---------- guessFollowsGraph ----------

    // Test case 1
    @Test
    public void testGuessFollowsGraphEmpty() {
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(new ArrayList<Tweet>());
        assertTrue("expected empty graph", graph.isEmpty());
    }

    // Test case 2
    @Test
    public void testGuessFollowsGraphNoMentions() {
        List<Tweet> tweets = Arrays.asList(
                tweet(1, "alice", "just had lunch"),
                tweet(2, "bob", "no mentions here either"));

        Map<String, Set<String>> graph = lowerCased(SocialNetwork.guessFollowsGraph(tweets));

        for (Set<String> follows : graph.values()) {
            assertTrue("nobody should follow anyone", follows.isEmpty());
        }
    }

    // Test case 3
    @Test
    public void testGuessFollowsGraphSingleMention() {
        List<Tweet> tweets = Arrays.asList(tweet(1, "alice", "hello @bob"));

        Map<String, Set<String>> graph = lowerCased(SocialNetwork.guessFollowsGraph(tweets));

        assertTrue("alice should be in the graph", graph.containsKey("alice"));
        assertTrue("alice should follow bob", graph.get("alice").contains("bob"));
    }

    // Test case 4
    @Test
    public void testGuessFollowsGraphMultipleMentions() {
        List<Tweet> tweets = Arrays.asList(tweet(1, "alice", "hi @bob and @carol and @dave"));

        Map<String, Set<String>> graph = lowerCased(SocialNetwork.guessFollowsGraph(tweets));

        assertTrue(graph.containsKey("alice"));
        assertTrue(graph.get("alice").containsAll(setOf("bob", "carol", "dave")));
    }

    // Test case 5
    @Test
    public void testGuessFollowsGraphMultipleTweetsSameUser() {
        List<Tweet> tweets = Arrays.asList(
                tweet(1, "alice", "hello @bob"),
                tweet(2, "alice", "see you @carol"),
                tweet(3, "alice", "again @bob"));

        Map<String, Set<String>> graph = lowerCased(SocialNetwork.guessFollowsGraph(tweets));

        assertTrue(graph.containsKey("alice"));
        assertTrue(graph.get("alice").containsAll(setOf("bob", "carol")));
    }

    @Test
    public void testGuessFollowsGraphMultipleAuthors() {
        List<Tweet> tweets = Arrays.asList(
                tweet(1, "alice", "hello @bob"),
                tweet(2, "carol", "hey @bob and @alice"));

        Map<String, Set<String>> graph = lowerCased(SocialNetwork.guessFollowsGraph(tweets));

        assertTrue(graph.get("alice").contains("bob"));
        assertTrue(graph.get("carol").containsAll(setOf("bob", "alice")));
    }

    @Test
    public void testGuessFollowsGraphCaseInsensitive() {
        List<Tweet> tweets = Arrays.asList(
                tweet(1, "Alice", "hello @BOB"),
                tweet(2, "ALICE", "hello again @bob"));

        Map<String, Set<String>> graph = lowerCased(SocialNetwork.guessFollowsGraph(tweets));

        assertTrue(graph.containsKey("alice"));
        assertTrue(graph.get("alice").contains("bob"));
    }

    @Test
    public void testGuessFollowsGraphSelfMention() {
        List<Tweet> tweets = Arrays.asList(tweet(1, "alice", "note to self @alice"));

        Map<String, Set<String>> graph = lowerCased(SocialNetwork.guessFollowsGraph(tweets));

        if (graph.containsKey("alice")) {
            assertFalse("users cannot follow themselves", graph.get("alice").contains("alice"));
        }
    }

    // ---------- influencers ----------

    // Test case 6
    @Test
    public void testInfluencersEmpty() {
        Map<String, Set<String>> graph = new HashMap<>();
        assertTrue("expected empty list", SocialNetwork.influencers(graph).isEmpty());
    }

    // Test case 7
    @Test
    public void testInfluencersSingleUserNoFollowers() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("alice", new HashSet<String>());

        assertTrue("nobody has followers", SocialNetwork.influencers(graph).isEmpty());
    }

    // Test case 8
    @Test
    public void testInfluencersSingleInfluencer() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("alice", setOf("bob"));

        List<String> influencers = SocialNetwork.influencers(graph);

        assertEquals(1, influencers.size());
        assertEquals("bob", influencers.get(0).toLowerCase());
    }

    // Test case 9
    @Test
    public void testInfluencersMultipleOrdered() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("alice", setOf("bob", "carol"));
        graph.put("dave", setOf("bob", "carol"));
        graph.put("erin", setOf("bob"));
        // followers: bob = 3, carol = 2

        List<String> influencers = SocialNetwork.influencers(graph);

        assertEquals(2, influencers.size());
        assertEquals("bob", influencers.get(0).toLowerCase());
        assertEquals("carol", influencers.get(1).toLowerCase());
    }

    // Test case 10
    @Test
    public void testInfluencersTied() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("alice", setOf("bob", "carol"));
        graph.put("dave", setOf("bob", "carol"));
        graph.put("erin", setOf("frank"));
        // followers: bob = 2, carol = 2, frank = 1

        List<String> influencers = SocialNetwork.influencers(graph);

        assertEquals(3, influencers.size());
        // the order of tied users is not specified, so only check the first two as a set
        Set<String> topTwo = new HashSet<>();
        topTwo.add(influencers.get(0).toLowerCase());
        topTwo.add(influencers.get(1).toLowerCase());
        assertEquals(setOf("bob", "carol"), topTwo);
        assertEquals("frank", influencers.get(2).toLowerCase());
    }

    @Test
    public void testInfluencersMixedCaseCountedTogether() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("alice", setOf("Bob"));
        graph.put("carol", setOf("BOB", "dave"));

        List<String> influencers = SocialNetwork.influencers(graph);

        assertEquals("bob", influencers.get(0).toLowerCase());
        assertEquals(2, influencers.size());
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * SocialNetwork class that follows the spec. It will be run against several
     * staff implementations of SocialNetwork, which will be done by overwriting
     * (temporarily) your version of SocialNetwork with the staff's version.
     * DO NOT strengthen the spec of SocialNetwork or its methods.
     *
     * In particular, your test cases must not call helper methods of your own
     * that you have put in SocialNetwork, because that means you're testing a
     * stronger spec than SocialNetwork says. If you need such helper methods,
     * define them in a different class. If you only need them in this test
     * class, then keep them in this test class.
     */
}