package ai_notes_api.controller;

import ai_notes_api.entity.Note;
import ai_notes_api.service.NoteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<Note> getAllNotes() {
        return noteService.getAllNotes();
    }

    // YENİ: JSON yerine form-data kabul ediyoruz
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Note> createNote(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            Note note = new Note();
            note.setTitle(title);
            note.setContent(content);
            return new ResponseEntity<>(noteService.saveNote(note, file), HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Note> updateNote(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            Note noteDetails = new Note();
            noteDetails.setTitle(title);
            noteDetails.setContent(content);
            return ResponseEntity.ok(noteService.updateNote(id, noteDetails, file));
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    // YENİ: PDF indirme uç noktası
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Note note = noteService.getAllNotes().stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Not bulunamadı"));

        if (note.getPdfData() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + note.getPdfName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(note.getPdfData());
    }
}