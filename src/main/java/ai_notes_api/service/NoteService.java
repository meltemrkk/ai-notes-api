package ai_notes_api.service;

import ai_notes_api.entity.Note;
import ai_notes_api.repository.NoteRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final AiService aiService;

    public NoteService(NoteRepository noteRepository, AiService aiService) {
        this.noteRepository = noteRepository;
        this.aiService = aiService;
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Note saveNote(Note note, MultipartFile file) throws IOException {
        String contentForAi = note.getContent();

        // 1. PDF İşlemi: Dosya varsa byte'a çevir ve içindeki metni oku
        if (file != null && !file.isEmpty()) {
            note.setPdfName(file.getOriginalFilename());
            note.setPdfData(file.getBytes());

            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                String extractedText = pdfStripper.getText(document);

                // Yapay zekaya gönderilecek içeriğe PDF metnini de ekliyoruz
                contentForAi = contentForAi + "\n\n--- EKLENEN PDF İÇERİĞİ ---\n" + extractedText;
            } catch (Exception e) {
                System.out.println("PDF metni okunurken hata oluştu: " + e.getMessage());
            }
        }

        // 2. Yapay Zeka İşlemleri: Hem kullanıcının notunu hem de PDF metnini birleşik olarak analiz et
        if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            String aiGeneratedTitle = aiService.getTitleFromAi(contentForAi);
            note.setTitle(aiGeneratedTitle);
        }

        String generatedSummary = aiService.getSummaryFromAi(contentForAi);
        note.setSummary(generatedSummary);

        // 3. Veritabanına Kaydet
        return noteRepository.save(note);
    }

    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }

    public Note updateNote(Long id, Note noteDetails, MultipartFile file) throws IOException {
        Note existingNote = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not bulunamadı! ID: " + id));

        String contentForAi = noteDetails.getContent();

        // 1. Yeni PDF yüklendiyse metni oku ve veritabanındaki dosyayı güncelle
        if (file != null && !file.isEmpty()) {
            existingNote.setPdfName(file.getOriginalFilename());
            existingNote.setPdfData(file.getBytes());

            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                String extractedText = pdfStripper.getText(document);
                contentForAi = contentForAi + "\n\n--- EKLENEN PDF İÇERİĞİ ---\n" + extractedText;
            } catch (Exception e) {
                System.out.println("PDF metni okunurken hata oluştu: " + e.getMessage());
            }
        }

        // 2. Yapay Zeka Analizi
        if (noteDetails.getTitle() == null || noteDetails.getTitle().trim().isEmpty()) {
            String aiGeneratedTitle = aiService.getTitleFromAi(contentForAi);
            existingNote.setTitle(aiGeneratedTitle);
        } else {
            existingNote.setTitle(noteDetails.getTitle());
        }

        existingNote.setContent(noteDetails.getContent());
        existingNote.setSummary(aiService.getSummaryFromAi(contentForAi));

        return noteRepository.save(existingNote);
    }
}