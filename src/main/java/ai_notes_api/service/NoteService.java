package ai_notes_api.service;

import ai_notes_api.entity.Note;
import ai_notes_api.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final AiService aiService;

    // Her iki servisi de (Veritabanı ve Yapay Zeka) içeri alıyoruz
    public NoteService(NoteRepository noteRepository, AiService aiService) {
        this.noteRepository = noteRepository;
        this.aiService = aiService;
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Note saveNote(Note note) {
        // 1. Dışarıdan gelen uzun notu AiService'e gönderip özetini alıyoruz
        String generatedSummary = aiService.getSummaryFromAi(note.getContent());

        // 2. Gelen özeti notumuzun "summary" kısmına ekliyoruz
        note.setSummary(generatedSummary);

        // 3. Artık özeti de olan notu veritabanına kaydediyoruz
        return noteRepository.save(note);
    }

    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }

    public Note updateNote(Long id, Note noteDetails) {
        Note existingNote = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not bulunamadı! ID: " + id));

        existingNote.setTitle(noteDetails.getTitle());
        existingNote.setContent(noteDetails.getContent());

        // İçerik güncellendiği için yapay zekaya yeni içeriği gönderip özeti de yeniliyoruz
        String newSummary = aiService.getSummaryFromAi(noteDetails.getContent());
        existingNote.setSummary(newSummary);

        return noteRepository.save(existingNote);
    }
}