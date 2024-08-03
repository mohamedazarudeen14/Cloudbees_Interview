package com.cloudbees.train.enums;

public enum TrainSection {
    SECTION_A("Section A", 1),
    SECTION_B("Section B", 2);

    private final Integer sectionId;
    private final String sectionName;

    TrainSection(String sectionName, int sectionId) {
        this.sectionName = sectionName;
        this.sectionId = sectionId;
    }

    public static TrainSection from(String sectionName) throws Exception {
        for (var value : values()) {
            if (value.sectionName.equalsIgnoreCase((sectionName))) {
                return value;
            }
        }
        throw new Exception("Section Not found in the train for the given sectionName");
    }

    public static TrainSection from(int sectionId) throws Exception {
        for (var value : values()) {
            if (value.sectionId == sectionId) {
                return value;
            }
        }
        throw new Exception("Section Not found in the train for the given sectionId");
    }

    public int getSectionId() {
        return sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }
}
