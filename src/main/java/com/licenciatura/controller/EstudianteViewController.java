package com.licenciatura.controller;

import com.licenciatura.model.Estudiante;
import com.licenciatura.repository.EstudianteRepository;
import com.licenciatura.service.EstudianteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/estudiantes")
public class EstudianteViewController {

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private EstudianteRepository estudianteRepository;

    // Añade esta anotación para métodos que ambos roles pueden acceder
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INVITADO')")
    @GetMapping("/lista")
    public String listarEstudiantes(Model model) {
        List<Estudiante> estudiantes = estudianteService.obtenerTodos();
        // Asegurarse de que el campo graduado esté inicializado
        estudiantes.forEach(estudiante -> {
            if (estudiante.getGraduado() == null) {
                estudiante.setGraduado(false);
            }
        });
        model.addAttribute("estudiantes", estudiantes);
        return "listaEstudiantes";  // Vista para mostrar la lista de estudiantes
    }

    @GetMapping("/eliminar_estudiante")
    public String eliminarEstudianteForm() {
        return "eliminar_estudiante";
    }

    // Añade esta anotación para métodos que solo el admin puede acceder
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/nuevo")
    public String mostrarFormularioDeAgregarEstudiante(Model model) {
        model.addAttribute("estudiante", new Estudiante());
        return "nuevoEstudiante";  // La vista que mostrará el formulario
    }

    // Agregar un nuevo estudiante
    @PostMapping("/guardar")
    public String guardarEstudiante(@ModelAttribute Estudiante estudiante) {
        estudianteService.guardar(estudiante);
        return "redirect:/estudiantes/lista";  // Redirige a la lista de estudiantes
    }

    // Buscar estudiantes por nombre
    @GetMapping("/buscar")
    public String buscarEstudiantes(@RequestParam("nombre") String nombre, Model model) {
        try {
            // Usar el servicio que ya tiene el normalize
            List<Estudiante> estudiantes = estudianteService.buscarPorNombre(nombre);
            
            System.out.println("Búsqueda por nombre: " + nombre);
            
            // Verificar null antes de usar size()
            if (estudiantes == null) {
                estudiantes = new ArrayList<>();
                System.out.println("Lista nula, creando nueva lista vacía");
            }
            
            System.out.println("Estudiantes encontrados: " + estudiantes.size());
            
            // Inicializar graduado si es necesario
            estudiantes.forEach(e -> {
                if (e.getGraduado() == null) {
                    e.setGraduado(false);
                }
            });

            model.addAttribute("estudiantes", estudiantes);
            return "resultadoBusqueda";
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            model.addAttribute("error", "Error en la búsqueda");
            model.addAttribute("estudiantes", new ArrayList<>());
            return "resultadoBusqueda";
        }
    }

    // Buscar estudiante por ID
    @GetMapping("/buscar/id")
    public String buscarEstudiantePorId(@RequestParam("id") Long id, Model model) {
        Estudiante estudiante = estudianteService.obtenerPorId(id);
        model.addAttribute("estudiante", estudiante);
        return "resultadoBusqueda";  // Vista para mostrar el resultado
    }

    @GetMapping("/buscar/formulario")
    public String mostrarFormularioDeBusqueda() {
        return "buscarEstudiante"; // Nombre del archivo HTML
    }

    @PostMapping("/eliminar")
    public String eliminarEstudiante(@RequestParam("id") Long id) {
        estudianteService.eliminar(id);  // Llama al servicio para eliminar
        return "redirect:/estudiantes/lista";  // Redirige a la lista de estudiantes después de la eliminación
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Estudiante estudiante = estudianteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("ID de estudiante inválido:" + id));
        model.addAttribute("estudiante", estudiante);
        return "editarEstudiante";
    }

    @PostMapping("/editar/{id}")
    public String actualizarEstudiante(@PathVariable Long id, @ModelAttribute Estudiante estudiante) {
        estudianteRepository.save(estudiante);
        return "redirect:/estudiantes/lista";
    }
}
