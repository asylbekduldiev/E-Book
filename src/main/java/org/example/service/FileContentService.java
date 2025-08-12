package org.example.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.model.Lesson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileContentService {
    private static final String LESSONS_FILE = "/content/lessons.json";
    private static final Gson gson = new Gson();
    
    private ObservableList<Lesson> lessons = FXCollections.observableArrayList();
    
    public FileContentService() {
        loadLessons();
    }
    
    public ObservableList<Lesson> getLessons() {
        return lessons;
    }
    
    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
        saveLessons();
    }
    
    public void removeLesson(Lesson lesson) {
        lessons.remove(lesson);
        saveLessons();
    }
    
    public void updateLesson(Lesson lesson) {
        int index = lessons.indexOf(lesson);
        if (index != -1) {
            lessons.set(index, lesson);
            saveLessons();
        }
    }
    
    private void loadLessons() {
        Task<List<Lesson>> loadTask = new Task<>() {
            @Override
            protected List<Lesson> call() throws Exception {
                try (InputStream is = getClass().getResourceAsStream(LESSONS_FILE);
                     Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    
                    Type listType = new TypeToken<List<Lesson>>(){}.getType();
                    return gson.fromJson(reader, listType);
                } catch (Exception e) {
                    // Если файл не найден, создаем демо-уроки
                    return createDemoLessons();
                }
            }
        };
        
        loadTask.setOnSucceeded(event -> {
            List<Lesson> loadedLessons = loadTask.getValue();
            if (loadedLessons != null) {
                lessons.setAll(loadedLessons);
            }
        });
        
        new Thread(loadTask).start();
    }
    
    private void saveLessons() {
        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // В реальном приложении здесь была бы логика сохранения
                // Для демо-версии просто логируем
                System.out.println("Lessons saved: " + lessons.size());
                return null;
            }
        };
        
        new Thread(saveTask).start();
    }
    
    private List<Lesson> createDemoLessons() {
        return List.of(
            new Lesson("Введение в Java", 
                "Java - это объектно-ориентированный язык программирования, разработанный Sun Microsystems в 1995 году.", 
                "Programming", "Beginner", 20),
            new Lesson("Основы ООП", 
                "Объектно-ориентированное программирование основано на концепции объектов, которые содержат данные и код.", 
                "Programming", "Intermediate", 30),
            new Lesson("JavaFX UI", 
                "JavaFX - это библиотека для создания современных пользовательских интерфейсов в Java.", 
                "UI Development", "Advanced", 45),
            new Lesson("Работа с данными", 
                "Изучим различные способы работы с данными: файлы, базы данных, JSON.", 
                "Data Management", "Intermediate", 35),
            new Lesson("Многопоточность", 
                "Многопоточность позволяет выполнять несколько задач одновременно в одном приложении.", 
                "Advanced Programming", "Advanced", 50)
        );
    }
    
    public ObservableList<Lesson> searchLessons(String query) {
        if (query == null || query.trim().isEmpty()) {
            return lessons;
        }
        
        String lowerQuery = query.toLowerCase();
        return lessons.filtered(lesson -> 
            lesson.getTitle().toLowerCase().contains(lowerQuery) ||
            lesson.getContent().toLowerCase().contains(lowerQuery) ||
            lesson.getCategory().toLowerCase().contains(lowerQuery) ||
            lesson.getTags().toLowerCase().contains(lowerQuery)
        );
    }
    
    public ObservableList<Lesson> getLessonsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return lessons;
        }
        
        return lessons.filtered(lesson -> 
            lesson.getCategory().equalsIgnoreCase(category)
        );
    }
}