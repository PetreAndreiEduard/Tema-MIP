import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FitZoneApp {

    public enum Intensity { LIGHT, MEDIUM, HARD }

    public static class FitnessClassInfo {
        private final String name;
        private Intensity intensity;
        private double basePrice;

        public FitnessClassInfo(String name, Intensity intensity, double basePrice) {
            this.name = Objects.requireNonNull(name);
            this.intensity = intensity;
            this.basePrice = basePrice;
        }

        public String getName() { return name; }
        public Intensity getIntensity() { return intensity; }
        public double getBasePrice() { return basePrice; }

        public void setIntensity(Intensity intensity) { this.intensity = intensity; }
        public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

        @Override
        public String toString() {
            return String.format("%s (intensitate: %s, preț bază: %.2f)", name, intensity, basePrice);
        }
    }

    public static abstract class Trainer {
        private static final AtomicInteger ID_GEN = new AtomicInteger(1);
        private final int id;
        private String name;
        private String email;
        private FitnessClassInfo specializesIn;

        protected Trainer(String name, String email, FitnessClassInfo specializesIn) {
            this.id = ID_GEN.getAndIncrement();
            this.name = name;
            this.email = email;
            this.specializesIn = specializesIn;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public FitnessClassInfo getSpecializesIn() { return specializesIn; }

        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }
        public void setSpecializesIn(FitnessClassInfo fci) { this.specializesIn = fci; }

        public abstract String getTrainerType();

        @Override
        public String toString() {
            String spec = (specializesIn == null) ? "neatribuit" : specializesIn.getName();
            return String.format("[%d] %s (%s) - %s - %s", id, name, getTrainerType(), email, spec);
        }
    }

    public static class PermanentTrainer extends Trainer {
        private double salary;

        public PermanentTrainer(String name, String email, FitnessClassInfo specializesIn, double salary) {
            super(name, email, specializesIn);
            this.salary = salary;
        }

        public double getSalary() { return salary; }
        public void setSalary(double salary) { this.salary = salary; }

        @Override
        public String getTrainerType() { return "Permanent"; }

        @Override
        public String toString() {
            return super.toString() + String.format(" (salariu: %.2f)", salary);
        }
    }

    public static class ExternalTrainer extends Trainer {
        private String company;
        private double hourlyRate;

        public ExternalTrainer(String name, String email, FitnessClassInfo specializesIn, String company, double hourlyRate) {
            super(name, email, specializesIn);
            this.company = company;
            this.hourlyRate = hourlyRate;
        }

        public String getCompany() { return company; }
        public double getHourlyRate() { return hourlyRate; }

        @Override
        public String getTrainerType() { return "External"; }

        @Override
        public String toString() {
            return super.toString() + String.format(" (companie: %s, tarif/h: %.2f)", company, hourlyRate);
        }
    }

    public interface PricingStrategy {
        double calculatePrice(SubscriptionData data);
    }

    public static class SubscriptionData {
        public final FitnessClassInfo chosenClass;
        public final int months;
        public final boolean isPremium;

        public SubscriptionData(FitnessClassInfo chosenClass, int months, boolean isPremium) {
            this.chosenClass = chosenClass;
            this.months = months;
            this.isPremium = isPremium;
        }
    }

    public static class DefaultPricingStrategy implements PricingStrategy {
        @Override
        public double calculatePrice(SubscriptionData data) {
            double base = (data.chosenClass == null) ? 0.0 : data.chosenClass.getBasePrice();
            double monthly = base;
            // intensity adjustment
            switch (data.chosenClass.getIntensity()) {
                case LIGHT: monthly *= 0.9; break;
                case MEDIUM: monthly *= 1.0; break;
                case HARD: monthly *= 1.2; break;
            }
            double subtotal = monthly * data.months;
            if (data.months >= 12) subtotal *= 0.85;
            else if (data.months >= 6) subtotal *= 0.92;

            if (data.isPremium) {
                subtotal *= 1.30;
            }
            return Math.round(subtotal * 100.0) / 100.0;
        }
    }

    public interface Subscription {
        String getSubscriberName();
        double getPrice();
        String brief();
    }

    public static class BasicSubscription implements Subscription {
        private static final AtomicInteger ID_GEN = new AtomicInteger(1);
        private final int id;
        private final String subscriberName;
        private final FitnessClassInfo fclass;
        private final int months;
        private final boolean premium;
        private final double price;

        public BasicSubscription(String subscriberName, FitnessClassInfo fclass, int months, boolean premium, PricingStrategy pricing) {
            this.id = ID_GEN.getAndIncrement();
            this.subscriberName = subscriberName;
            this.fclass = fclass;
            this.months = months;
            this.premium = premium;
            this.price = pricing.calculatePrice(new SubscriptionData(fclass, months, premium));
        }

        public int getId() { return id; }
        @Override public String getSubscriberName() { return subscriberName; }
        public FitnessClassInfo getFclass() { return fclass; }
        public int getMonths() { return months; }
        @Override public double getPrice() { return price; }
        public boolean isPremium() { return premium; }

        @Override
        public String brief() {
            String cls = (fclass == null) ? "N/A" : fclass.getName();
            return String.format("[%d] %s - %s - %d luni - %s - %.2f", id, subscriberName, cls, months, premium ? "Premium" : "Standard", price);
        }

        @Override public String toString() { return brief(); }
    }

    public static class TrainerRepository {
        private final List<Trainer> trainers = new ArrayList<>();
        public void add(Trainer t) { trainers.add(t); }
        public List<Trainer> list() { return Collections.unmodifiableList(trainers); }
        public Optional<Trainer> findById(int id) {
            return trainers.stream().filter(t -> t.getId() == id).findFirst();
        }
    }

    public static class ClassRepository {
        private final List<FitnessClassInfo> classes = new ArrayList<>();
        public void add(FitnessClassInfo c) { classes.add(c); }
        public List<FitnessClassInfo> list() { return Collections.unmodifiableList(classes); }
        public Optional<FitnessClassInfo> findByName(String name) {
            return classes.stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst();
        }
    }

    public static class SubscriptionRepository {
        private final List<Subscription> subs = new ArrayList<>();
        public void add(Subscription s) { subs.add(s); }
        public List<Subscription> list() { return Collections.unmodifiableList(subs); }
    }

    private final Scanner scanner = new Scanner(System.in);
    private final TrainerRepository trainerRepo = new TrainerRepository();
    private final ClassRepository classRepo = new ClassRepository();
    private final SubscriptionRepository subRepo = new SubscriptionRepository();
    private final PricingStrategy pricing = new DefaultPricingStrategy();

    public static void main(String[] args) {
        FitZoneApp app = new FitZoneApp();
        app.seedSampleData();
        app.run();
    }

    private void seedSampleData() {
        classRepo.add(new FitnessClassInfo("Yoga", Intensity.LIGHT, 30.0));
        classRepo.add(new FitnessClassInfo("CrossFit", Intensity.HARD, 45.0));
        classRepo.add(new FitnessClassInfo("Pilates", Intensity.MEDIUM, 35.0));
        trainerRepo.add(new PermanentTrainer("Ana Popescu", "ana@fitzone.ro", classRepo.list().get(0), 2500.0));
        trainerRepo.add(new ExternalTrainer("Carlos Silva", "carlos@trainco.com", classRepo.list().get(1), "TrainCo", 60.0));
    }

    private void run() {
        boolean exit = false;
        while (!exit) {
            printMenu();
            String choice = input("Alege opțiunea: ");
            switch (choice) {
                case "1": addTrainer(); break;
                case "2": addClassType(); break;
                case "3": createSubscription(); break;
                case "4": listTrainers(); break;
                case "5": listClassTypes(); break;
                case "6": listSubscriptions(); break;
                case "7": generateReport(); break;
                case "0": exit = true; break;
                default: println("Opțiune invalidă."); break;
            }
            println("");
        }
        println("La revedere!");
    }

    private void printMenu() {
        println("=== FitZone+ Manager ===");
        println("1) Adăugare antrenor");
        println("2) Adăugare tip clasă");
        println("3) Creare abonament client");
        println("4) Afișare antrenori");
        println("5) Afișare tipuri de clasă");
        println("6) Afișare abonamente");
        println("7) Generare raport sumar (clase + antrenori)");
        println("0) Ieșire");
    }

    private void addTrainer() {
        println("--- Adăugare antrenor ---");
        String name = input("Nume: ");
        String email = input("Email: ");
        println("Tip antrenor: 1) Permanent  2) External");
        String t = input("Alege tip (1/2): ");
        FitnessClassInfo spec = chooseClassOptional();
        if ("1".equals(t)) {
            double salary = readDouble("Salariu lunar (ex: 2500): ");
            PermanentTrainer pt = new PermanentTrainer(name, email, spec, salary);
            trainerRepo.add(pt);
            println("Antrenor permanent adăugat: " + pt);
        } else {
            String company = input("Companie/firmă: ");
            double hr = readDouble("Tarif oră (ex: 50): ");
            ExternalTrainer et = new ExternalTrainer(name, email, spec, company, hr);
            trainerRepo.add(et);
            println("Antrenor external adăugat: " + et);
        }
    }

    private void addClassType() {
        println("--- Adăugare tip clasă ---");
        String name = input("Nume clasă (ex: Spinning intensiv): ");
        println("Intensitate: 1) LIGHT  2) MEDIUM  3) HARD");
        String i = input("Alege intensitate (1/2/3): ");
        Intensity intensity = Intensity.MEDIUM;
        if ("1".equals(i)) intensity = Intensity.LIGHT;
        else if ("3".equals(i)) intensity = Intensity.HARD;
        double base = readDouble("Preț bază lunar (ex: 40.0): ");
        FitnessClassInfo fci = new FitnessClassInfo(name, intensity, base);
        classRepo.add(fci);
        println("Tip clasă adăugat: " + fci);
    }

    private void createSubscription() {
        println("--- Creare abonament ---");
        String subName = input("Nume client: ");
        FitnessClassInfo chosen = chooseClassRequired();
        int months = readInt("Număr luni (ex: 1, 6, 12): ");
        println("Tip abonament: 1) Standard  2) Premium");
        String t = input("Alege tip (1/2): ");
        boolean premium = "2".equals(t);
        BasicSubscription sub = new BasicSubscription(subName, chosen, months, premium, pricing);
        subRepo.add(sub);
        println("Abonament creat: " + sub.brief());
    }

    private void listTrainers() {
        println("--- Antrenori ---");
        List<Trainer> list = trainerRepo.list();
        if (list.isEmpty()) println("Niciun antrenor înregistrat.");
        else list.forEach(t -> println(t.toString()));
    }

    private void listClassTypes() {
        println("--- Tipuri de clasă ---");
        List<FitnessClassInfo> list = classRepo.list();
        if (list.isEmpty()) println("Niciun tip de clasă definit.");
        else list.forEach(c -> println(c.toString()));
    }

    private void listSubscriptions() {
        println("--- Abonamente ---");
        List<Subscription> list = subRepo.list();
        if (list.isEmpty()) println("Niciun abonament creat.");
        else list.forEach(s -> println(s.brief()));
    }

    private void generateReport() {
        println("=== Raport sumar: Tipuri de antrenamente și antrenori ===");
        Map<String, List<Trainer>> map = new TreeMap<>();
        for (FitnessClassInfo f : classRepo.list()) {
            map.put(f.getName(), new ArrayList<>());
        }
        map.put("Neatribuite", new ArrayList<>());

        for (Trainer t : trainerRepo.list()) {
            FitnessClassInfo fci = t.getSpecializesIn();
            if (fci == null) {
                map.get("Neatribuite").add(t);
            } else {
                map.computeIfAbsent(fci.getName(), k -> new ArrayList<>()).add(t);
            }
        }

        for (Map.Entry<String, List<Trainer>> e : map.entrySet()) {
            println("Clasă: " + e.getKey());
            if (e.getValue().isEmpty()) {
                println("  (fără antrenori)");
            } else {
                for (Trainer tt : e.getValue()) {
                    println("  - " + tt.getName() + " (" + tt.getTrainerType() + ")");
                }
            }
        }
    }

    private FitnessClassInfo chooseClassOptional() {
        List<FitnessClassInfo> list = classRepo.list();
        if (list.isEmpty()) {
            println("Nu există tipuri de clasă. (adaugă mai întâi)");
            return null;
        }
        println("Alege una din clase (sau lasă gol pentru niciuna):");
        for (int i = 0; i < list.size(); i++) {
            println((i + 1) + ") " + list.get(i));
        }
        String choice = input("Număr (sau Enter): ");
        if (choice.trim().isEmpty()) return null;
        try {
            int idx = Integer.parseInt(choice) - 1;
            if (idx >= 0 && idx < list.size()) return list.get(idx);
        } catch (NumberFormatException ignored) {}
        println("Alegere invalidă; nicio clasă asignată.");
        return null;
    }

    private FitnessClassInfo chooseClassRequired() {
        List<FitnessClassInfo> list = classRepo.list();
        if (list.isEmpty()) {
            println("Nu există tipuri de clasă. Adăugați una mai întâi.");
            return null;
        }
        println("Alege clasa:");
        for (int i = 0; i < list.size(); i++) {
            println((i + 1) + ") " + list.get(i));
        }
        while (true) {
            String choice = input("Număr: ");
            try {
                int idx = Integer.parseInt(choice) - 1;
                if (idx >= 0 && idx < list.size()) return list.get(idx);
            } catch (NumberFormatException ignored) {}
            println("Alegere invalidă. Încearcă din nou.");
        }
    }

    private String input(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private void println(String s) { System.out.println(s); }

    private double readDouble(String prompt) {
        while (true) {
            String v = input(prompt);
            try { return Double.parseDouble(v); }
            catch (NumberFormatException e) { println("Număr invalid. Reîncearcă."); }
        }
    }

    private int readInt(String prompt) {
        while (true) {
            String v = input(prompt);
            try { return Integer.parseInt(v); }
            catch (NumberFormatException e) { println("Număr invalid. Reîncearcă."); }
        }
    }
}
