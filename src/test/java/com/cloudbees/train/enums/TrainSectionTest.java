package com.cloudbees.train.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainSectionTest {
    @Test
    void should_throw_exception_when_wrong_section_name_passed() {
        assertThatCode(() -> TrainSection.from("Test")).isInstanceOf(Exception.class)
                .hasMessage("Section Not found in the train for the given sectionName");
    }

    @Test
    void should_throw_exception_when_wrong_section_id_passed() {
        assertThatCode(() -> TrainSection.from(5)).isInstanceOf(Exception.class)
                .hasMessage("Section Not found in the train for the given sectionId");
    }

    @Test
    void should_get_section_with_passing_valid_name() throws Exception {
        TrainSection trainSection = TrainSection.from("Section A");

        assertThat(trainSection.getSectionId()).isEqualTo(1);
    }

    @Test
    void should_get_section_with_passing_valid_id() throws Exception {
        TrainSection trainSection = TrainSection.from(1);

        assertThat(trainSection.getSectionName()).isEqualTo("Section A");
    }

    @Test
    void should_get_train_section_id() {
        TrainSection section = TrainSection.SECTION_A;
        var sectionId = section.getSectionId();

        assertEquals(sectionId, section.getSectionId());
    }

    @Test
    void should_get_train_section_name() {
        TrainSection section = TrainSection.SECTION_B;
        var sectionName = section.getSectionName();

        assertEquals(sectionName, section.getSectionName());
    }
}