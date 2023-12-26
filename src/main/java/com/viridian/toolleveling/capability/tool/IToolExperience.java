package com.viridian.toolleveling.capability.tool;

public interface IToolExperience {
    /**
     * Retrieves the current experience points.
     *
     * @return Current experience points.
     */
    int getExperience();

    /**
     * Sets the current experience points.
     *
     * @param experience New experience points.
     */
    void setExperience(int experience);

    /**
     * Adds experience points.
     *
     * @param experienceToAdd The amount of experience to add.
     */
    void addExperience(int experienceToAdd);

    /**
     * Retrieves the current level of the tool.
     *
     * @return Current level.
     */
    int getLevel();

    /**
     * Sets the current level of the tool.
     *
     * @param level New level of the tool.
     */
    void setLevel(int level);

    /**
     * Increments the level of the tool, typically called when an experience threshold is reached.
     */
    void levelUp();

    /**
     * Determines if the tool is eligible for a level up based on its current experience.
     *
     * @return True if the tool can level up, false otherwise.
     */
    boolean canLevelUp();

    // You can define additional behaviors relevant to tool experience here.
}
