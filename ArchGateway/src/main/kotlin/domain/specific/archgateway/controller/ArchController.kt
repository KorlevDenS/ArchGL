package domain.specific.archgateway.controller

import domain.specific.archgateway.data.ArchGLProgram
import domain.specific.archgateway.data.TextUmlRequest
import domain.specific.archgateway.service.ArchGenService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/arch/gateway/")
class ArchController(private val archGenService: ArchGenService) {

    @PostMapping("generate")
    fun generateArch(@RequestBody program: ArchGLProgram): ResponseEntity<TextUmlRequest> {
        return ResponseEntity.ok(TextUmlRequest(archGenService.generateArch(program.program)))
    }

}
