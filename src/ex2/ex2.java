package ex2;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

class Produs {
    private String denumire;
    private double pret;
    private int cantitate;
    private LocalDate dataExpirarii;
    private static double incasari = 0;

    public Produs(String denumire, double pret, int cantitate, LocalDate dataExpirarii) {
        this.denumire = denumire;
        this.pret = pret;
        this.cantitate = cantitate;
        this.dataExpirarii = dataExpirarii;
    }

    public String getDenumire() {
        return denumire;
    }

    public double getPret() {
        return pret;
    }

    public int getCantitate() {
        return cantitate;
    }

    public void setCantitate(int cantitate) {
        this.cantitate = cantitate;
    }

    public LocalDate getDataExpirarii() {
        return dataExpirarii;
    }

    public static double getIncasari() {
        return incasari;
    }

    public static void adaugaIncasari(double valoare) {
        incasari += valoare;
    }

    @Override
    public String toString() {
        return String.format("Produs: %s, Preț: %.2f, Cantitate: %d, Data expirării: %s",
                denumire, pret, cantitate, dataExpirarii);
    }
}

public class ex2 {
    private static List<Produs> produse = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            citireProduseDinFisier("produse.csv");
        } catch (IOException e) {
            System.err.println("Eroare la citirea fișierului: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n=== Meniu Magazin ===");
            System.out.println("1. Afișarea tuturor produselor");
            System.out.println("2. Afișarea produselor expirate");
            System.out.println("3. Vânzarea unui produs");
            System.out.println("4. Afișarea produselor cu preț minim");
            System.out.println("5. Salvarea produselor cu cantitate mai mică decât o valoare dată");
            System.out.println("6. Ieșire");
            System.out.print("Alegeți o opțiune: ");

            int optiune = scanner.nextInt();
            scanner.nextLine(); // Consumăm newline

            switch (optiune) {
                case 1:
                    afisareProduse();
                    break;
                case 2:
                    afisareProduseExpirate();
                    break;
                case 3:
                    vanzareProdus(scanner);
                    break;
                case 4:
                    afisareProdusePretMinim();
                    break;
                case 5:
                    salvareProduseCuCantitateRedusa(scanner);
                    break;
                case 6:
                    System.out.println("Ieșire. Încasări totale: " + Produs.getIncasari());
                    return;
                default:
                    System.out.println("Opțiune invalidă.");
            }
        }
    }

    private static void citireProduseDinFisier(String fileName) throws IOException {
        List<String> linii = Files.readAllLines(Paths.get(fileName));
        for (String linie : linii) {
            String[] valori = linie.split(",");
            String denumire = valori[0].trim();
            double pret = Double.parseDouble(valori[1].trim());
            int cantitate = Integer.parseInt(valori[2].trim());
            LocalDate dataExpirarii = LocalDate.parse(valori[3].trim());

            produse.add(new Produs(denumire, pret, cantitate, dataExpirarii));
        }
    }

    private static void afisareProduse() {
        System.out.println("\nLista produselor:");
        produse.forEach(System.out::println);
    }

    private static void afisareProduseExpirate() {
        System.out.println("\nProduse expirate:");
        LocalDate azi = LocalDate.now();
        produse.stream()
                .filter(p -> p.getDataExpirarii().isBefore(azi))
                .forEach(System.out::println);
    }

    private static void vanzareProdus(Scanner scanner) {
        System.out.print("Introduceți denumirea produsului: ");
        String denumire = scanner.nextLine();

        Optional<Produs> produs = produse.stream()
                .filter(p -> p.getDenumire().equalsIgnoreCase(denumire))
                .findFirst();

        if (produs.isPresent()) {
            Produs p = produs.get();
            System.out.print("Introduceți cantitatea de vândut: ");
            int cantitateDeVandut = scanner.nextInt();
            scanner.nextLine(); // Consumăm newline

            if (cantitateDeVandut > p.getCantitate()) {
                System.out.println("Cantitate insuficientă pe stoc.");
            } else {
                double incasariVanzare = cantitateDeVandut * p.getPret();
                Produs.adaugaIncasari(incasariVanzare);
                p.setCantitate(p.getCantitate() - cantitateDeVandut);

                System.out.printf("Produs vândut cu succes. Încasări: %.2f\n", incasariVanzare);

                if (p.getCantitate() == 0) {
                    produse.remove(p);
                    System.out.println("Produs eliminat din listă deoarece cantitatea este zero.");
                }
            }
        } else {
            System.out.println("Produsul nu a fost găsit.");
        }
    }

    private static void afisareProdusePretMinim() {
        OptionalDouble pretMinim = produse.stream()
                .mapToDouble(Produs::getPret)
                .min();

        if (pretMinim.isPresent()) {
            double minim = pretMinim.getAsDouble();
            produse.stream()
                    .filter(p -> p.getPret() == minim)
                    .forEach(System.out::println);
        } else {
            System.out.println("Nu există produse.");
        }
    }

    private static void salvareProduseCuCantitateRedusa(Scanner scanner) {
        System.out.print("Introduceți cantitatea limită: ");
        int limita = scanner.nextInt();
        scanner.nextLine(); // Consumăm newline

        List<Produs> produseFiltrate = produse.stream()
                .filter(p -> p.getCantitate() < limita)
                .collect(Collectors.toList());

        try (PrintWriter writer = new PrintWriter("produse_filtrate.txt")) {
            for (Produs p : produseFiltrate) {
                writer.println(p);
            }
            System.out.println("Produsele au fost salvate în 'produse_filtrate.txt'.");
        } catch (IOException e) {
            System.err.println("Eroare la salvarea fișierului: " + e.getMessage());
        }
    }
}
