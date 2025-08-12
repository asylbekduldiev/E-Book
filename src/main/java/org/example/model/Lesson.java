package org.example.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Lesson {
    private final StringProperty title;
    private final StringProperty content;
    private final StringProperty category;
    private final StringProperty difficulty;
    private final IntegerProperty estimatedTime;
    private final ObjectProperty<LocalDateTime> lastAccessed;
    private final BooleanProperty isCompleted;
    private final StringProperty tags;

    public Lesson(String title, String content) {
        this.title = new SimpleStringProperty(title);
        this.content = new SimpleStringProperty(content);
        this.category = new SimpleStringProperty("General");
        this.difficulty = new SimpleStringProperty("Beginner");
        this.estimatedTime = new SimpleIntegerProperty(15);
        this.lastAccessed = new SimpleObjectProperty<>(LocalDateTime.now());
        this.isCompleted = new SimpleBooleanProperty(false);
        this.tags = new SimpleStringProperty("");
    }

    public Lesson(String title, String content, String category, String difficulty, int estimatedTime) {
        this.title = new SimpleStringProperty(title);
        this.content = new SimpleStringProperty(content);
        this.category = new SimpleStringProperty(category);
        this.difficulty = new SimpleStringProperty(difficulty);
        this.estimatedTime = new SimpleIntegerProperty(estimatedTime);
        this.lastAccessed = new SimpleObjectProperty<>(LocalDateTime.now());
        this.isCompleted = new SimpleBooleanProperty(false);
        this.tags = new SimpleStringProperty("");
    }

    // Getters and Setters
    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }
    public StringProperty titleProperty() { return title; }

    public String getContent() { return content.get(); }
    public void setContent(String content) { this.content.set(content); }
    public StringProperty contentProperty() { return content; }

    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }
    public StringProperty categoryProperty() { return category; }

    public String getDifficulty() { return difficulty.get(); }
    public void setDifficulty(String difficulty) { this.difficulty.set(difficulty); }
    public StringProperty difficultyProperty() { return difficulty; }

    public int getEstimatedTime() { return estimatedTime.get(); }
    public void setEstimatedTime(int estimatedTime) { this.estimatedTime.set(estimatedTime); }
    public IntegerProperty estimatedTimeProperty() { return estimatedTime; }

    public LocalDateTime getLastAccessed() { return lastAccessed.get(); }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed.set(lastAccessed); }
    public ObjectProperty<LocalDateTime> lastAccessedProperty() { return lastAccessed; }

    public boolean isCompleted() { return isCompleted.get(); }
    public void setCompleted(boolean completed) { isCompleted.set(completed); }
    public BooleanProperty completedProperty() { return isCompleted; }

    public String getTags() { return tags.get(); }
    public void setTags(String tags) { this.tags.set(tags); }
    public StringProperty tagsProperty() { return tags; }

    @Override
    public String toString() {
        return getTitle();
    }
}