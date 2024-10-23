
/**
 * 
 * @author İbrahim Güldemir ibrahim.guldemir@ogr.sakarya.edu.tr
 * @since 01.04.2024
 * <p>
 * Bu sınıf analiz işlemlerine sahip sınıftır çekilen repo içindeki java dosyalarının üzerinde işlem yapacak sınıftır isteenilen şeyleri sağlayacak hesaplama fonksiyonlarına sahiptir 
 * <p>
 */


import java.io.*;
import java.nio.file.*;
import java.util.regex.*;

//analiz sınıfı
public class Analyzer {
    private static int javadocLines = 0;
    private static int otherComments = 0;
    private static int codeLines = 0;
    private static int totalLines = 0;
    private static int functionCount = 0;

    //dosya içindeki github reposundan çekilen dosyaları inceleyen fonksiyon
    public static void analyzeJavaFilesInDirectory(Path directoryPath) throws IOException {
    Files.walk(directoryPath)
        .filter(path -> path.toString().endsWith(".java"))
        .forEach(javaFile -> {
            try {
                String content = new String(Files.readAllBytes(javaFile));
                if (content.contains("class") && !isItInterface(content)) {
                    analyzeJavaFile(javaFile,content);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }); 
}
    //eğer dosyanın içinde herhangi bir interface varsa o sınıfı dahil etmemesini sağlamak için fonksiyon
    private static boolean isItInterface(String content) {
            // Regex deseni oluştur
            Pattern pattern = Pattern.compile("(?:.*)?\\b(?:public|private|protected)\\s+\\binterface\\s+(\\w+)\\b");
            Matcher matcher = pattern.matcher(content);
            
            // Eşleşmeyi kontrol et
            return matcher.find();
    }
    //dosyadaki fonksiyon sayılarını bulan fonksiyon
    private static void fonkCounter(String content) {
        Pattern fonkPattern = Pattern.compile("(?:.*)?\\b(?:public|private|protected|abstract)\\s+(static)*(?:\\w+(?:\\s*<[\\w\\s,]+>|\\[\\])?(?:\\s+\\w+)?(?:\\s+<\\w+(?:\\s*,\\s*\\w+)*>)?|[\\w]+\\s*)\\s*(\\w+)\\s*\\([^)]*\\)\\s*(?:throws\\s+\\w+(?:\\s*,\\s*\\w+)*)?\\s*\\{\r\n"
        		+ "");
        Matcher fonkMatcher = fonkPattern.matcher(content);
        while (fonkMatcher.find()) {
            functionCount++;
        }
    }
    // yorum satırlarını bulan fonksiyon 
    private static void commentCounter(String content){

        Pattern pattern = Pattern.compile("//.*");
        Matcher matcher = pattern.matcher(content);

        // Sınıf içindeki yorum satırlarını bul // ile olanları
        while (matcher.find()) {
            String comment = matcher.group();
            if (!comment.startsWith("/**")) { // Javadoc yorumlarını hariç tut
                otherComments++;
            }
        }
        // sınıf içerisindeki /**/ yorum satırlarını bulan kod parçası
        String[] lines = content.split("\\R");
        boolean insideMultilineComment = false;

        for (String line : lines) {
        	String trimmedLine = line.trim();
            if (!insideMultilineComment && (trimmedLine.startsWith("/*") && !trimmedLine.startsWith("/**"))) {
               
            	if(trimmedLine.endsWith("*/")) {
                	otherComments++;
                	continue;
                }
                else{
                    insideMultilineComment = true;  
                    continue;
                }
            }
            
            if(insideMultilineComment && !trimmedLine.contains("*/")) {
            	codeLines--;
            	otherComments++;
            }
            if (insideMultilineComment && trimmedLine.contains("*/")) {
                insideMultilineComment = false;
                codeLines-=2;
            }
        }
        //string içerisindeki herhangi bir yorum satırısı varsa bulması için bir denetim
        pattern = Pattern.compile("[\\\"'](.*?)\\/\\/(.*?)[\\\"']");
        matcher = pattern.matcher(content);

        int strComment = 0;
        while (matcher.find()) {
            String stringLiteral = matcher.group();
            strComment += countCommentsInString(stringLiteral);
        }
        otherComments-=strComment; // string içerisindeki yorum satırlarını ana satırdan çıkarıp gerçek yorum satırı sayısını bulan kod
    }
    //string içindeki yorum satırlarını sayan fonksiyon
    private static int countCommentsInString(String str) {
        Pattern pattern = Pattern.compile("//.*|/\\*(?:[^\"\\\\]|\\\\.)*?\\*/");
        Matcher matcher = pattern.matcher(str);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }
    //javadocları hesaplayan fonksiyon
    private static void javadocCounter(String content) {
    	Pattern javadocPattern = Pattern.compile("/\\*\\*(.|\\R)*?\\*/", Pattern.MULTILINE | Pattern.DOTALL);
		Matcher javadocMatcher = javadocPattern.matcher(content);
		int commentLines=0;
		while (javadocMatcher.find()) {
			String match = javadocMatcher.group();
			String[] lines = match.split("\\R");
			// Subtract start and end lines
			commentLines = lines.length - 2;
			if (commentLines > 0) {
				javadocLines += commentLines;
			}
		}
		
		String noStringsContent = content.replaceAll("\".*?\"", "\"\"");
		//kaç tane javadoc olduğunu bulan kod
		javadocPattern = Pattern.compile("/\\*\\*(.|\\R)*?\\*/", Pattern.MULTILINE | Pattern.DOTALL);
		javadocMatcher = javadocPattern.matcher(noStringsContent);
		int javadocCount = 0;
		while (javadocMatcher.find()) {
			javadocCount++;
		}
		codeLines=codeLines-javadocCount*2-javadocLines;// kod satırlarından bu javadoc sayılarının çıkarılması

		//toplam kod satırı bulan fonksiyon
    }private static void codeLineCounter(String content) {
    	String[] lines = content.split("\\r?\\n");
		for (String line : lines) {
			line = line.trim();
			if (!line.isEmpty() && !line.startsWith("//") && !line.matches("/\\*.*?\\*/")) {
				codeLines++;
			}
		}
    }
    //toplam satır sayısını bulan fonksiyon
    private static void totalLineCounter(String content) {
    	String[] lines = content.split("\\r?\\n");
		for (String line : lines) {
			line = line.trim();
			totalLines++;
		}
    }
    
    // diğer fonksiyonları kullanarak java dosyasını analiz eden fonksiyon
    private static void analyzeJavaFile(Path javaFile,String content) {
    	integerRefresher();
        fonkCounter(content);
		codeLineCounter(content);
        commentCounter(content);
		javadocCounter(content);
		totalLineCounter(content);
		
		int tumYorumlar=javadocLines + otherComments;
		showResults(javaFile,tumYorumlar);
    }
    //her bir java dosyası için değerleri sıfırlayan fonk
    private static void integerRefresher()
    {
        javadocLines = 0;
        otherComments = 0;
        codeLines = 0;
        totalLines = 0;    
        functionCount = 0;
    }
    //sonuçları yazdıran fonksiyon
    public static void showResults(Path javaFile,int tumYorumlar) {
        System.out.println("Java Dosyası: " + javaFile.getFileName());
        System.out.println("Javadoc Satır Sayısı: " + javadocLines);
        System.out.println("Diğer yorum satırları: " + otherComments);
        System.out.println("Kod Satır Sayısı: " + codeLines);
        System.out.println("LOC: " + totalLines);
        System.out.println("Fonksiyon Sayısı: " + functionCount);
        
        double YG = ((tumYorumlar) * 0.8) / functionCount;
		double YH = (codeLines / (double) functionCount) * 0.3;
		double yorumSapmaYuzdesi = ((100 * YG) / YH) - 100;
		
        System.out.println("Yorum Sapma Yuzdesi: %"+String.format("%.2f", yorumSapmaYuzdesi));
        System.out.println("------------------------------------------");
    }
}