# Dokumentacja Projektu NextRep

## 1. Cel Projektu
NextRep to nowoczesna aplikacja do śledzenia treningów siłowych, skupiająca się na prostocie obsługi podczas treningu oraz możliwości elastycznego planowania sesji.

## 2. Architektura
Projekt opiera się na architekturze **Clean Architecture** z podziałem na warstwy:
- **Data**: Implementacja bazy danych (Room) oraz repozytoriów.
- **Domain**: Logika biznesowa, interfejsy repozytoriów i modele danych.
- **Presentation**: Widoki (Jetpack Compose) oraz ViewModels.

---

## 3. Struktury Danych i Trwałość

Aplikacja wykorzystuje hierarchiczną strukturę danych do reprezentacji planu treningowego, a dane są trwale przechowywane przy użyciu biblioteki Room.

### A. Workout (Trening/Plan) - Encja Room
- **Typ struktury:** Klasa danych oznaczona `@Entity` (`workout_plans`).
- **Pola:** Zawiera m.in. `id` (PrimaryKey), `exercises` (MutableList) oraz `scheduledDays` (Set).
- **Trwałość:** Zapisywana w lokalnej bazie danych SQLite. Typy złożone (listy, zbiory) są serializowane do formatu JSON za pomocą `TypeConverters` (GSON).

### B. Exercise (Ćwiczenie)
- **Typ struktury:** Klasa danych zawierająca listę serii.
- **Służy do:** Definiowania konkretnego ruchu (np. "Wyciskanie").
- **Działanie:** Obsługuje dwa typy: `REPS_AND_WEIGHT` (siłowe) oraz `TIME` (np. plank).

### C. ExerciseSet (Seria)
- **Typ struktury:** Klasa danych.
- **Służy do:** Rejestrowania konkretnych wyników (ciężar, powtórzenia lub czas).

---

## 4. Wykorzystane Wzorce Projektowe i Mechanizmy

### A. DAO (Data Access Object)
- **Teoria:** Wzorzec DAO oddziela logikę dostępu do danych od restzty aplikacji. Definiuje czysty interfejs API, przez który można komunikować się z bazą danych, ukrywając szczegóły implementacyjne (np. konkretne zapytania SQL).
- **W projekcie:** Interfejs `WorkoutDao` w `app/src/main/java/com/example/nextrep/data/local/` definiuje metody takie jak `insertWorkout` i `getAllPlans`, zwracając dane jako `Flow`, co integruje go ze wzorcem Observer.

### B. Wzorzec Strategia (Strategy Pattern)
- **Teoria:** Pozwala na zdefiniowanie rodziny algorytmów, umieszczenie każdego z nich w oddzielnej klasie i uczynienie ich obiektów wymienialnymi. Dzięki temu algorytm może być zmieniany dynamicznie w trakcie działania programu.
- **W projekcie:** Interfejs `ScoringStrategy` w `app/src/main/java/com/example/nextrep/domain/logic/` pozwala na podmienianie sposobu liczenia punktów za trening (np. raz może to być `VolumeStrategy`, a w przyszłości `IntensityStrategy`), bez zmiany kodu, który z niego korzysta.

### C. Wzorzec Obserwator (Observer Pattern)
- **Teoria:** Umożliwia subskrybowanie i otrzymywanie powiadomień o zmianach stanu obiektu. Obiekt (podmiot) utrzymuje listę swoich obserwatorów i automatycznie ich powiadamia o wszelkich zmianach.
- **W projekcie:** `WorkoutViewModel` eksponuje dane jako `StateFlow`, które jest strumieniem danych (podmiotem). Ekrany w Jetpack Compose używają `collectAsState()`, aby stać się obserwatorami. Gdy dane w `Flow` się zmieniają (np. po operacji na bazie), UI jest automatycznie i reaktywnie odświeżane.

### D. Type Converters (jako forma wzorca Adapter)
- **Teoria:** Wzorzec Adapter pozwala na współpracę obiektów o niekompatybilnych interfejsach. Konwerter typów działa jak adapter, tłumacząc dane z formatu niezrozumiałego dla jednej części systemu na format zrozumiały dla drugiej.
- **W projekcie:** Room nie wie, jak zapisać obiekt `List<Exercise>` w kolumnie bazy danych. `Converters.kt` w `app/src/main/java/com/example/nextrep/data/local/` działają jak adapter, który konwertuje listę na format tekstowy (JSON) i z powrotem, umożliwiając trwały zapis złożonych struktur danych.

---

## 5. Logika Obliczania "Streak" (Ciąg treningowy)
Aplikacja implementuje zaawansowaną logikę obliczania ciągłości treningowej:
- **Pauza w Rest Day:** Streak nie resetuje się ani nie rośnie w dni, które nie są zaplanowane jako treningowe.
- **Weryfikacja Planu:** System sprawdza `scheduledDays` we wszystkich planach użytkownika. Jeśli dzień jest planowany, a trening nie został ukończony, streak jest przerywany (z wyjątkiem bieżącego dnia).
- **Automatyczny Reset:** Jeśli trening zostanie ukończony, system automatycznie przygotowuje świeżą sesję na następny zaplanowany dzień.

---

## 6. System Postępów i Statystyk
Aplikacja oferuje rozbudowany system analizy wyników:
- **Karty Porównawcze:** Dla każdego ćwiczenia generowane jest zestawienie najlepszego wyniku (PR) z dnia dzisiejszego, sprzed tygodnia oraz sprzed miesiąca.
- **Wykres Tygodniowy:** Wizualizacja sumarycznej objętości treningowej (`Total Volume`) z ostatnich 7 dni, ułatwiająca śledzenie systematyczności.
- **Metodologia PR:** Wynik obliczany jako `Ciężar × Powtórzenia` dla najlepszej serii w danej sesji.

---

## 7. Struktura Modułów
- **:app**
    - `data/local/`: Konfiguracja Room (`AppDatabase.kt`, `WorkoutDao.kt`, `Converters.kt`).
    - `domain/models/`: `TrainingModels.kt`, `ProgressModels.kt` - Definicje encji i modeli danych.
    - `presentation/`: Ekrany (`MainScreen.kt`, `ProgressScreen.kt`, `WorkoutScreenContent.kt`) i `WorkoutViewModel.kt`.
    - `ui/theme/`: Definicje wizualne (GorillaGreen, GymBlack).

---

## 8. Status Rozwoju
- [x] Implementacja bazy danych Room (z obsługą KSP i migracją).
- [x] Reaktywne odświeżanie listy planów (Flow + collectAsState).
- [x] Pełny kreator planów z zapisem do bazy.
- [x] System edycji i usuwania planów.
- [x] Obsługa treningów na czas i siłowych.
- [x] Logika obliczania wyniku treningu (Strategy).
- [x] Zaawansowany licznik "Streak" z obsługą dni wolnych (Rest Days).
- [x] Nowy ekran "Postępy" z kartami PR i wykresem tygodniowym.
- [x] Obsługa preserii (WARMUP) dla pierwszego ćwiczenia w treningu.
- [x] Poprawiona obsługa pól tekstowych (TextFieldValue) zapobiegająca skakaniu kursora.
