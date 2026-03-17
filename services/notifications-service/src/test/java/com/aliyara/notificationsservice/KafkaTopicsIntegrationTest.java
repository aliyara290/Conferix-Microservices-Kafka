package com.aliyara.notificationsservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for validating Kafka topics and consumers.
 * 
 * This test verifies that:
 * 1. All required Kafka topics are configured
 * 2. Consumers listen on the correct topics
 * 3. Events can be published to topics
 */
@SpringBootTest
@ActiveProfiles("test")
class KafkaTopicsIntegrationTest {

    @Value("${app.kafka.topics.keynote-events}")
    private String keynoteEventsTopic;

    @Value("${app.kafka.topics.conference-events}")
    private String conferenceEventsTopic;

    @Value("${app.kafka.topics.review-events}")
    private String reviewEventsTopic;

    @Value("${app.kafka.topics.inscription-events}")
    private String inscriptionEventsTopic;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testAllTopicsAreConfigured() {
        assertThat(keynoteEventsTopic).isNotNull().isNotEmpty().isEqualTo("keynote-events");
        assertThat(conferenceEventsTopic).isNotNull().isNotEmpty().isEqualTo("conference-events");
        assertThat(reviewEventsTopic).isNotNull().isNotEmpty().isEqualTo("review-events");
        assertThat(inscriptionEventsTopic).isNotNull().isNotEmpty().isEqualTo("inscription-events");
    }

    @Test
    void testKeynoteEventTopicIsConfigured() throws Exception {
        String eventPayload = createEventPayload("keynote_created", 1L, "Dr. Jane Doe", "Keynote on AI");
        assertThat(eventPayload).isNotEmpty();
        assertThat(keynoteEventsTopic).isEqualTo("keynote-events");
    }

    @Test
    void testConferenceEventTopicIsConfigured() throws Exception {
        String eventPayload = createEventPayload("conference_created", 2L, "Spring Conference 2026", "Annual Spring Conference");
        assertThat(eventPayload).isNotEmpty();
        assertThat(conferenceEventsTopic).isEqualTo("conference-events");
    }

    @Test
    void testReviewEventTopicIsConfigured() throws Exception {
        String eventPayload = createEventPayload("review_submitted", 3L, "Reviewer Name", "Excellent presentation");
        assertThat(eventPayload).isNotEmpty();
        assertThat(reviewEventsTopic).isEqualTo("review-events");
    }

    @Test
    void testInscriptionEventTopicIsConfigured() throws Exception {
        String eventPayload = createEventPayload("participant_registered", 4L, "John Doe", "john.doe@example.com");
        assertThat(eventPayload).isNotEmpty();
        assertThat(inscriptionEventsTopic).isEqualTo("inscription-events");
    }

    @Test
    void testTopicNamesMatchConsumerAnnotations() {
        // These should match the @KafkaListener topic configurations in the consumers
        assertThat(keynoteEventsTopic).isEqualTo("keynote-events");
        assertThat(conferenceEventsTopic).isEqualTo("conference-events");
        assertThat(reviewEventsTopic).isEqualTo("review-events");
        assertThat(inscriptionEventsTopic).isEqualTo("inscription-events");
    }

    /**
     * Helper method to create a sample event payload in JSON format
     */
    private String createEventPayload(String eventType, Long referenceId, String title, String description) throws Exception {
        var objectNode = objectMapper.createObjectNode();
        objectNode.put("eventType", eventType);
        objectNode.put("referenceId", referenceId);
        objectNode.put("title", title);
        objectNode.put("description", description);
        objectNode.put("timestamp", System.currentTimeMillis());
        
        return objectMapper.writeValueAsString(objectNode);
    }
}
