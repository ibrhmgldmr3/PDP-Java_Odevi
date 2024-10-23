
/**
 * 
 * @author İbrahim Güldemir ibrahim.guldemir@ogr.sakarya.edu.tr
 * @since 01.04.2024
 * <p>
 * Bu sınıf main sınıfımız github reposunu çektiğimiz ve üzerinde işlemleri yaptığımız sınıftır
 * <p>
 */



import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Github Repo URL'sini Giriniz:");
        String repoUrl = scanner.nextLine();
        String localPath = "..\\git clonee";  // klonlanacak repo için konum

        // eğer dosya varsa içerisindekileri sil
        Path path = Paths.get(localPath);
        try {
            if (Files.exists(path)) {
                Files.walk(path)
                    .map(Path::toFile)
                    .sorted((o1, o2) -> -o1.compareTo(o2))
                    .forEach(File::delete);
            }
        } catch (IOException e) {
            System.out.println("Konumdaki dosyalar silinirken hata oluştu!");
            e.printStackTrace();
        }

        // github reposunu klonlama işlemleri
        try {
            ProcessBuilder builder = new ProcessBuilder("git", "clone", repoUrl, localPath);
            builder.inheritIO();  
            Process process = builder.start();
            process.waitFor();
            System.out.println("\nTamamlandı!\n");
        } catch (IOException | InterruptedException e) {
            System.out.println("Repo kopyalanırken hata oluştu!"); 
            e.printStackTrace();
        }
        try {
            Analyzer.analyzeJavaFilesInDirectory(Paths.get(localPath));
        } catch (IOException e) {
            System.out.println("Dosya analiz edilirken hata oluştu!");
            e.printStackTrace();
        }
        
        scanner.close();
    }
}