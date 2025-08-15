package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.geometry.Insets;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.Lesson;
import org.example.service.FileContentService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.stage.Stage;

public class MainController implements Initializable {
    
    @FXML private VBox mainContainer;
    @FXML private HBox topBar;
    @FXML private VBox sidebar;
    @FXML private VBox contentArea;
    @FXML private TextField searchField;
    @FXML private ListView<String> contentListView;
    @FXML private WebView contentView;
    @FXML private Label sectionTitleLabel;
    @FXML private Label sectionDescriptionLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressText;
    @FXML private Button lectureButton;
    @FXML private Button practiceButton;
    @FXML private Button tasksButton;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button completeButton;
    
    private FileContentService fileService;
    private JsonObject lessonData;
    private ObservableList<String> lectureContent;
    private ObservableList<String> practiceContent;
    private ObservableList<String> tasksContent;
    private String currentSection = "lecture";
    private int currentContentIndex = 0;
    
    // Отдельный прогресс для каждой секции
    private int completedLectureItems = 0;
    private int completedPracticeItems = 0;
    private int completedTasksItems = 0;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fileService = new FileContentService();
        
        loadContent();
        setupUI();
        setupEventHandlers();
        
        // Показываем секцию лекции по умолчанию
        showLectureSection();
    }
    
    private void setupUI() {
        // Настройка поиска
        searchField.setPromptText("Введите ключевые слова...");
        searchField.getStyleClass().add("search-field");
        
        // Настройка списка содержимого
        contentListView.setCellFactory(param -> new ContentListCell());
        contentListView.getStyleClass().add("content-list");
        
        // Настройка WebView для контента
        contentView.setContextMenuEnabled(false);
        contentView.getEngine().setUserStyleSheetLocation(getClass().getResource("/css/content.css").toExternalForm());
        
        // Настройка прогресса
        updateProgress();
    }
    
    private void setupEventHandlers() {
        // Поиск
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterContent(newVal);
            }
        });
        
        // Выбор элемента содержимого
        contentListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showContent(newVal);
                currentContentIndex = getCurrentContentList().indexOf(newVal);
                updateNavigationButtons();
            }
        });
    }
    
    private void loadContent() {
        try {
            // Загружаем данные из JSON
            String jsonContent = new String(getClass().getResourceAsStream("/content/lessons.json").readAllBytes());
            Gson gson = new Gson();
            
            // JSON это массив, берем первый элемент
            com.google.gson.JsonElement[] lessons = gson.fromJson(jsonContent, com.google.gson.JsonElement[].class);
            if (lessons.length > 0) {
                lessonData = lessons[0].getAsJsonObject();
            }
            
            // Загружаем содержимое для каждой секции
            loadLectureContent();
            loadPracticeContent();
            loadTasksContent();
            
        } catch (Exception e) {
            e.printStackTrace();
            // Создаем демо-контент если не удалось загрузить
            createDemoContent();
        }
    }
    
    private void loadLectureContent() {
        lectureContent = FXCollections.observableArrayList();
        if (lessonData != null && lessonData.has("lecture")) {
            JsonObject lecture = lessonData.getAsJsonObject("lecture");
            JsonArray sections = lecture.getAsJsonArray("sections");
            for (int i = 0; i < sections.size(); i++) {
                JsonObject section = sections.get(i).getAsJsonObject();
                lectureContent.add(section.get("title").getAsString());
            }
        }
    }
    
    private void loadPracticeContent() {
        practiceContent = FXCollections.observableArrayList();
        if (lessonData != null && lessonData.has("practice")) {
            JsonObject practice = lessonData.getAsJsonObject("practice");
            JsonArray sections = practice.getAsJsonArray("sections");
            for (int i = 0; i < sections.size(); i++) {
                JsonObject section = sections.get(i).getAsJsonObject();
                practiceContent.add(section.get("title").getAsString());
            }
        }
    }
    
    private void loadTasksContent() {
        tasksContent = FXCollections.observableArrayList();
        if (lessonData != null && lessonData.has("tasks")) {
            JsonObject tasks = lessonData.getAsJsonObject("tasks");
            JsonArray sections = tasks.getAsJsonArray("sections");
            for (int i = 0; i < sections.size(); i++) {
                JsonObject section = sections.get(i).getAsJsonObject();
                tasksContent.add(section.get("title").getAsString());
            }
        }
    }
    
    private void createDemoContent() {
        // Демо-контент если JSON не загрузился
        lectureContent = FXCollections.observableArrayList(
            "Что такое JavaFX?",
            "Ключевые преимущества",
            "Архитектура JavaFX",
            "Система событий",
            "Инструменты разработки"
        );
        
        practiceContent = FXCollections.observableArrayList(
            "Создание простого приложения",
            "Использование FXML",
            "CSS стилизация",
            "Анимации и переходы",
            "Работа с данными и привязка"
        );
        
        tasksContent = FXCollections.observableArrayList(
            "Задача 1: Создание калькулятора",
            "Задача 2: Приложение для заметок",
            "Задача 3: Игра \"Угадай число\"",
            "Задача 4: Менеджер файлов",
            "Задача 5: Графический редактор",
            "Бонусная задача: Медиа-плеер"
        );
    }
    
    @FXML
    private void showLectureSection() {
        switchSection("lecture", lectureButton);
        sectionTitleLabel.setText("Лекция");
        sectionDescriptionLabel.setText("Идеально удобно для чтения");
        contentListView.setItems(lectureContent);
        showSectionContent("lecture");
    }
    
    @FXML
    private void showPracticeSection() {
        switchSection("practice", practiceButton);
        sectionTitleLabel.setText("Практика");
        sectionDescriptionLabel.setText("Примеры использования с блоками кода");
        contentListView.setItems(practiceContent);
        showSectionContent("practice");
    }
    
    @FXML
    private void showTasksSection() {
        switchSection("tasks", tasksButton);
        sectionTitleLabel.setText("Задачи");
        sectionDescriptionLabel.setText("Практические задания для закрепления");
        contentListView.setItems(tasksContent);
        showSectionContent("tasks");
    }
    
    private void switchSection(String section, Button activeButton) {
        currentSection = section;
        currentContentIndex = 0;
        
        // Обновляем стили кнопок - ВСЕ ОДИНАКОВЫЕ
        lectureButton.getStyleClass().remove("active");
        practiceButton.getStyleClass().remove("active");
        tasksButton.getStyleClass().remove("active");
        
        // Добавляем активный стиль только к выбранной кнопке
        activeButton.getStyleClass().add("active");
        
        // Сбрасываем выбор в списке
        contentListView.getSelectionModel().clearSelection();
        
        // Обновляем навигацию и прогресс для новой секции
        updateNavigationButtons();
        updateProgress();
    }
    
    private void showSectionContent(String section) {
        String htmlContent = createSectionContent(section);
        contentView.getEngine().loadContent(htmlContent);
        animateContentAppearance();
    }
    
    private void showContent(String contentTitle) {
        String htmlContent = createDetailedContent(contentTitle, currentSection);
        contentView.getEngine().loadContent(htmlContent);
        animateContentAppearance();
    }
    
    @FXML
    private void showPreviousContent() {
        ObservableList<String> currentList = getCurrentContentList();
        if (currentContentIndex > 0) {
            currentContentIndex--;
            String prevItem = currentList.get(currentContentIndex);
            contentListView.getSelectionModel().select(prevItem);
            showContent(prevItem);
        }
    }
    
    @FXML
    private void showNextContent() {
        ObservableList<String> currentList = getCurrentContentList();
        if (currentContentIndex < currentList.size() - 1) {
            currentContentIndex++;
            String nextItem = currentList.get(currentContentIndex);
            contentListView.getSelectionModel().select(nextItem);
            showContent(nextItem);
        }
    }
    
    @FXML
    private void markAsCompleted() {
        // Получаем текущий выбранный элемент
        String selectedItem = contentListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Увеличиваем прогресс для текущей секции
            switch (currentSection) {
                case "lecture":
                    if (completedLectureItems < lectureContent.size()) {
                        completedLectureItems++;
                    }
                    break;
                case "practice":
                    if (completedPracticeItems < practiceContent.size()) {
                        completedPracticeItems++;
                    }
                    break;
                case "tasks":
                    if (completedTasksItems < tasksContent.size()) {
                        completedTasksItems++;
                    }
                    break;
            }
            
            updateProgress();
            
            // Анимация успеха
            completeButton.getStyleClass().add("completed");
            completeButton.setText("Изучено!");
            
            // Через 2 секунды возвращаем обычный текст
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        completeButton.getStyleClass().remove("completed");
                        completeButton.setText("Отметить как изученное");
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    @FXML
    private void exportTasksToExcel() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Tasks");

            if (lessonData != null && lessonData.has("tasks")) {
                JsonArray sections = lessonData
                        .getAsJsonObject("tasks")
                        .getAsJsonArray("sections");

                // Заголовки столбцов
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Title");
                header.createCell(1).setCellValue("Description");
                header.createCell(2).setCellValue("Difficulty");
                header.createCell(3).setCellValue("Time");
                header.createCell(4).setCellValue("Requirements");

                // Заполнение строк
                for (int i = 0; i < sections.size(); i++) {
                    JsonObject task = sections.get(i).getAsJsonObject();
                    Row row = sheet.createRow(i + 1);
                    row.createCell(0).setCellValue(task.get("title").getAsString());
                    row.createCell(1).setCellValue(task.get("description").getAsString());
                    row.createCell(2).setCellValue(task.get("difficulty").getAsString());
                    row.createCell(3).setCellValue(task.get("time").getAsString());

                    StringBuilder reqs = new StringBuilder();
                    JsonArray requirements = task.getAsJsonArray("requirements");
                    for (int j = 0; j < requirements.size(); j++) {
                        reqs.append(requirements.get(j).getAsString());
                        if (j < requirements.size() - 1) reqs.append(", ");
                    }
                    row.createCell(4).setCellValue(reqs.toString());
                }
            }

            // Сохраняем через FileChooser
            FileChooser saveChooser = new FileChooser();
            saveChooser.setTitle("Сохранить задачи в Excel");
            saveChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            File saveFile = saveChooser.showSaveDialog(mainContainer.getScene().getWindow());

            if (saveFile != null) {
                try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                    workbook.write(fos);
                }
                workbook.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @FXML
    private void minimizeWindow() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.setIconified(true);
    }
    
    @FXML
    private void maximizeWindow() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        if (stage.isMaximized()) {
            stage.setMaximized(false);
        } else {
            stage.setMaximized(true);
        }
    }
    
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.close();
    }
    
    // Метод для сброса прогресса (можно добавить кнопку)
    public void resetProgress() {
        completedLectureItems = 0;
        completedPracticeItems = 0;
        completedTasksItems = 0;
        updateProgress();
    }
    
    // Метод для получения общего прогресса по всем секциям
    public double getOverallProgress() {
        int totalItems = lectureContent.size() + practiceContent.size() + tasksContent.size();
        int totalCompleted = completedLectureItems + completedPracticeItems + completedTasksItems;
        return totalItems > 0 ? (double) totalCompleted / totalItems : 0.0;
    }
    
    private void updateProgress() {
        int totalItems = 0;
        int completedItems = 0;
        
        // Получаем прогресс для текущей секции
        switch (currentSection) {
            case "lecture":
                totalItems = lectureContent.size();
                completedItems = completedLectureItems;
                break;
            case "practice":
                totalItems = practiceContent.size();
                completedItems = completedPracticeItems;
                break;
            case "tasks":
                totalItems = tasksContent.size();
                completedItems = completedTasksItems;
                break;
        }
        
        double progress = totalItems > 0 ? (double) completedItems / totalItems : 0.0;
        
        progressBar.setProgress(progress);
        progressText.setText(String.format("%d/%d (%.0f%%)", completedItems, totalItems, progress * 100));
    }
    
    private void updateNavigationButtons() {
        ObservableList<String> currentList = getCurrentContentList();
        prevButton.setDisable(currentContentIndex == 0);
        nextButton.setDisable(currentContentIndex == currentList.size() - 1);
    }
    
    private ObservableList<String> getCurrentContentList() {
        switch (currentSection) {
            case "lecture": return lectureContent;
            case "practice": return practiceContent;
            case "tasks": return tasksContent;
            default: return lectureContent;
        }
    }
    
    private String createSectionContent(String section) {
        switch (section) {
            case "lecture":
                return createLectureContent();
            case "practice":
                return createPracticeContent();
            case "tasks":
                return createTasksContent();
            default:
                return createLectureContent();
        }
    }
    
    private String createLectureContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>JavaFX Лекция</title></head><body>");
        html.append("<div class='lecture-section'>");
        html.append("<h1>JavaFX - Современная разработка UI</h1>");
        
        if (lessonData != null && lessonData.has("lecture")) {
            JsonObject lecture = lessonData.getAsJsonObject("lecture");
            JsonArray sections = lecture.getAsJsonArray("sections");
            
            for (int i = 0; i < sections.size(); i++) {
                JsonObject section = sections.get(i).getAsJsonObject();
                String title = section.get("title").getAsString();
                String content = section.get("content").getAsString();
                
                html.append("<h2>").append(title).append("</h2>");
                html.append("<p>").append(content).append("</p>");
            }
        }
        
        html.append("</div></body></html>");
        return html.toString();
    }
    
    private String createPracticeContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>JavaFX Практика</title></head><body>");
        html.append("<div class='practice-section'>");
        html.append("<h1>Практика + Примеры использования</h1>");
        
        if (lessonData != null && lessonData.has("practice")) {
            JsonObject practice = lessonData.getAsJsonObject("practice");
            JsonArray sections = practice.getAsJsonArray("sections");
            
            for (int i = 0; i < sections.size(); i++) {
                JsonObject section = sections.get(i).getAsJsonObject();
                String title = section.get("title").getAsString();
                String content = section.get("content").getAsString();
                String code = section.has("code") ? section.get("code").getAsString() : "";
                
                html.append("<h2>").append(i + 1).append(". ").append(title).append("</h2>");
                html.append("<p>").append(content).append("</p>");
                
                if (!code.isEmpty()) {
                    html.append("<pre><code>").append(code).append("</code></pre>");
                }
            }
        }
        
        html.append("</div></body></html>");
        return html.toString();
    }
    
    private String createTasksContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>JavaFX Задачи</title></head><body>");
        html.append("<div class='tasks-section'>");
        html.append("<h1>Задачи для практики</h1>");
        
        if (lessonData != null && lessonData.has("tasks")) {
            JsonObject tasks = lessonData.getAsJsonObject("tasks");
            JsonArray sections = tasks.getAsJsonArray("sections");
            
            for (int i = 0; i < sections.size(); i++) {
                JsonObject section = sections.get(i).getAsJsonObject();
                String title = section.get("title").getAsString();
                String description = section.get("description").getAsString();
                String difficulty = section.get("difficulty").getAsString();
                String time = section.get("time").getAsString();
                JsonArray requirements = section.getAsJsonArray("requirements");
                
                html.append("<h2>").append(title).append("</h2>");
                html.append("<div class='task-card'>");
                html.append("<p><strong>Описание:</strong> ").append(description).append("</p>");
                
                if (requirements != null) {
                    html.append("<p><strong>Требования:</strong></p><ul>");
                    for (int j = 0; j < requirements.size(); j++) {
                        html.append("<li>").append(requirements.get(j).getAsString()).append("</li>");
                    }
                    html.append("</ul>");
                }
                
                html.append("<p><strong>Сложность:</strong> ").append(difficulty).append("</p>");
                html.append("<p><strong>Время выполнения:</strong> ").append(time).append("</p>");
                html.append("</div>");
            }
        }
        
        html.append("</div></body></html>");
        return html.toString();
    }
    
    private String createDetailedContent(String contentTitle, String section) {
        switch (section) {
            case "lecture":
                return createDetailedLectureContent(contentTitle);
            case "practice":
                return createDetailedPracticeContent(contentTitle);
            case "tasks":
                return createDetailedTasksContent(contentTitle);
            default:
                return createDetailedLectureContent(contentTitle);
        }
    }
    
    private String createDetailedLectureContent(String title) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>").append(title).append("</title></head><body>");
        html.append("<div class='lecture-section'>");
        html.append("<h1>").append(title).append("</h1>");
        
        if (lessonData != null && lessonData.has("lecture")) {
            JsonObject lecture = lessonData.getAsJsonObject("lecture");
            JsonArray sections = lecture.getAsJsonArray("sections");
            
            for (int i = 0; i < sections.size(); i++) {
                JsonObject section = sections.get(i).getAsJsonObject();
                String sectionTitle = section.get("title").getAsString();
                String content = section.get("content").getAsString();
                
                if (sectionTitle.equals(title)) {
                    html.append("<div class='content-block'>");
                    html.append("<h2>").append(sectionTitle).append("</h2>");
                    html.append("<p>").append(content).append("</p>");
                    
                    // Добавляем дополнительную информацию
                    html.append("<div class='info-box'>");
                    html.append("<h3>Ключевые моменты:</h3>");
                    html.append("<ul>");
                    if (title.contains("JavaFX")) {
                        html.append("<li>Современная платформа для UI</li>");
                        html.append("<li>Пришла на смену Swing</li>");
                        html.append("<li>Поддержка FXML и CSS</li>");
                    } else if (title.contains("преимущества")) {
                        html.append("<li>FXML - декларативное описание</li>");
                        html.append("<li>CSS стилизация</li>");
                        html.append("<li>Встроенные анимации</li>");
                    } else if (title.contains("Архитектура")) {
                        html.append("<li>Scene Graph - основа</li>");
                        html.append("<li>Stage, Scene, Node</li>");
                        html.append("<li>Эффективная отрисовка</li>");
                    } else if (title.contains("событий")) {
                        html.append("<li>Observer паттерн</li>");
                        html.append("<li>Интерактивность</li>");
                        html.append("<li>Отзывчивый интерфейс</li>");
                    } else if (title.contains("Инструменты")) {
                        html.append("<li>Scene Builder</li>");
                        html.append("<li>IntelliJ IDEA</li>");
                        html.append("<li>Eclipse и NetBeans</li>");
                    }
                    html.append("</ul>");
                    html.append("</div>");
                    
                    html.append("</div>");
                    break;
                }
            }
        }
        
        html.append("</div></body></html>");
        return html.toString();
    }
    
    private String createDetailedPracticeContent(String title) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>").append(title).append("</title></head><body>");
        html.append("<div class='practice-section'>");
        html.append("<h1>").append(title).append("</h1>");
        
        if (lessonData != null && lessonData.has("practice")) {
            JsonObject practice = lessonData.getAsJsonObject("practice");
            JsonArray sections = practice.getAsJsonArray("sections");
            
            for (int i = 0; i < sections.size(); i++) {
                JsonObject section = sections.get(i).getAsJsonObject();
                String sectionTitle = section.get("title").getAsString();
                String content = section.get("content").getAsString();
                String code = section.has("code") ? section.get("code").getAsString() : "";
                
                if (sectionTitle.equals(title)) {
                    html.append("<div class='content-block'>");
                    html.append("<h2>").append(sectionTitle).append("</h2>");
                    html.append("<p>").append(content).append("</p>");
                    
                    if (!code.isEmpty()) {
                        html.append("<div class='code-example'>");
                        html.append("<h3>Пример кода:</h3>");
                        html.append("<pre><code>").append(code).append("</code></pre>");
                        html.append("</div>");
                    }
                    
                    // Добавляем практические советы
                    html.append("<div class='practice-tips'>");
                    html.append("<h3>Практические советы:</h3>");
                    html.append("<ul>");
                    if (title.contains("приложения")) {
                        html.append("<li>Начните с простой кнопки</li>");
                        html.append("<li>Используйте VBox для вертикального расположения</li>");
                        html.append("<li>Не забывайте про main метод</li>");
                    } else if (title.contains("FXML")) {
                        html.append("<li>Отделяйте логику от представления</li>");
                        html.append("<li>Используйте fx:controller</li>");
                        html.append("<li>Проверяйте соответствие ID в FXML и контроллере</li>");
                    } else if (title.contains("CSS")) {
                        html.append("<li>Используйте -fx- префикс для свойств</li>");
                        html.append("<li>Создавайте отдельные CSS файлы</li>");
                        html.append("<li>Применяйте стили через styleClass</li>");
                    } else if (title.contains("Анимации")) {
                        html.append("<li>Начинайте с простых FadeTransition</li>");
                        html.append("<li>Используйте Duration для контроля времени</li>");
                        html.append("<li>Комбинируйте разные типы анимаций</li>");
                    } else if (title.contains("данными")) {
                        html.append("<li>Используйте Property классы</li>");
                        html.append("<li>Привязывайте данные через bind</li>");
                        html.append("<li>Следите за жизненным циклом объектов</li>");
                    }
                    html.append("</ul>");
                    html.append("</div>");
                    
                    html.append("</div>");
                    break;
                }
            }
        }
        
        html.append("</div></body></html>");
        return html.toString();
    }
    
    private String createDetailedTasksContent(String title) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>").append(title).append("</title></head><body>");
        html.append("<div class='tasks-section'>");
        html.append("<h1>").append(title).append("</h1>");
        
        if (lessonData != null && lessonData.has("tasks")) {
            JsonObject tasks = lessonData.getAsJsonObject("tasks");
            JsonArray sections = tasks.getAsJsonArray("sections");
            
            for (int i = 0; i < sections.size(); i++) {
                JsonObject section = sections.get(i).getAsJsonObject();
                String sectionTitle = section.get("title").getAsString();
                String description = section.get("description").getAsString();
                String difficulty = section.get("difficulty").getAsString();
                String time = section.get("time").getAsString();
                JsonArray requirements = section.getAsJsonArray("requirements");
                
                if (sectionTitle.equals(title)) {
                    html.append("<div class='content-block'>");
                    html.append("<div class='task-header'>");
                    html.append("<h2>").append(sectionTitle).append("</h2>");
                    html.append("<div class='task-meta'>");
                    html.append("<span class='difficulty'>Сложность: ").append(difficulty).append("</span>");
                    html.append("<span class='time'>Время: ").append(time).append("</span>");
                    html.append("</div>");
                    html.append("</div>");
                    
                    html.append("<div class='task-description'>");
                    html.append("<p>").append(description).append("</p>");
                    html.append("</div>");
                    
                    if (requirements != null) {
                        html.append("<div class='task-requirements'>");
                        html.append("<h3>Требования:</h3>");
                        html.append("<ul>");
                        for (int j = 0; j < requirements.size(); j++) {
                            html.append("<li>").append(requirements.get(j).getAsString()).append("</li>");
                        }
                        html.append("</ul>");
                        html.append("</div>");
                    }
                    
                    // Добавляем подсказки и советы
                    html.append("<div class='task-tips'>");
                    html.append("<h3>Подсказки для выполнения:</h3>");
                    html.append("<ul>");
                    if (title.contains("калькулятор")) {
                        html.append("<li>Используйте GridPane для расположения кнопок</li>");
                        html.append("<li>Создайте отдельный класс для логики вычислений</li>");
                        html.append("<li>Добавьте обработку ошибок деления на ноль</li>");
                    } else if (title.contains("заметок")) {
                        html.append("<li>Используйте SplitPane для разделения интерфейса</li>");
                        html.append("<li>Создайте модель данных для заметок</li>");
                        html.append("<li>Добавьте автоматическое сохранение</li>");
                    } else if (title.contains("Угадай число")) {
                        html.append("<li>Используйте Random для генерации числа</li>");
                        html.append("<li>Добавьте анимации при победе</li>");
                        html.append("<li>Создайте красивый счетчик попыток</li>");
                    } else if (title.contains("файлов")) {
                        html.append("<li>Используйте TreeView для папок</li>");
                        html.append("<li>Добавьте контекстное меню</li>");
                        html.append("<li>Реализуйте drag & drop</li>");
                    } else if (title.contains("редактор")) {
                        html.append("<li>Создайте Canvas для рисования</li>");
                        html.append("<li>Добавьте палитру инструментов</li>");
                        html.append("<li>Реализуйте Undo/Redo</li>");
                    } else if (title.contains("Медиа-плеер")) {
                        html.append("<li>Используйте MediaPlayer для воспроизведения</li>");
                        html.append("<li>Создайте плейлист с ListView</li>");
                        html.append("<li>Добавьте визуализацию аудио</li>");
                    }
                    html.append("</ul>");
                    html.append("</div>");
                    
                    html.append("</div>");
                    break;
                }
            }
        }
        
        html.append("</div></body></html>");
        return html.toString();
    }
    
    private String createContentHtml(String contentTitle, String section) {
        // Создаем детальное содержимое для выбранного элемента
        return createSectionContent(section);
    }
    
    private void filterContent(String query) {
        if (query.trim().isEmpty()) {
            switch (currentSection) {
                case "lecture" -> contentListView.setItems(lectureContent);
                case "practice" -> contentListView.setItems(practiceContent);
                case "tasks" -> contentListView.setItems(tasksContent);
            }
        } else {
            ObservableList<String> filteredContent = FXCollections.observableArrayList();
            ObservableList<String> currentContent = null;
            
            switch (currentSection) {
                case "lecture" -> currentContent = lectureContent;
                case "practice" -> currentContent = practiceContent;
                case "tasks" -> currentContent = tasksContent;
            }
            
            if (currentContent != null) {
                currentContent.stream()
                    .filter(item -> item.toLowerCase().contains(query.toLowerCase()))
                    .forEach(filteredContent::add);
                contentListView.setItems(filteredContent);
            }
        }
    }
    
    private void animateContentAppearance() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), contentArea);
        fadeIn.setFromValue(0.3);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), contentArea);
        scaleIn.setFromX(0.95);
        scaleIn.setToX(1.0);
        scaleIn.setFromY(0.95);
        scaleIn.setToY(1.0);
        scaleIn.play();
    }
    
    // Кастомная ячейка для списка содержимого
    private static class ContentListCell extends ListCell<String> {
        private final VBox container;
        private final Label titleLabel;
        
        public ContentListCell() {
            container = new VBox(5);
            container.setPadding(new Insets(15));
            container.getStyleClass().add("content-cell");
            
            titleLabel = new Label();
            titleLabel.getStyleClass().add("content-title");
            titleLabel.setWrapText(true);
            
            container.getChildren().add(titleLabel);
        }
        
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setGraphic(null);
            } else {
                titleLabel.setText(item);
                setGraphic(container);
            }
        }
    }
}