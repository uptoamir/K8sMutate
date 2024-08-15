package com.uptoamir.K8sMutate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import io.fabric8.kubernetes.api.model.admission.v1.AdmissionReview;
import io.fabric8.kubernetes.api.model.admission.v1.AdmissionResponse;
import io.fabric8.kubernetes.api.model.admission.v1.AdmissionRequest;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.api.model.Pod;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mutate")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("#{${mutations}}")
    private List<Map<String, String>> mutations;

    @PostMapping("/")
    public AdmissionReview mutate(@RequestBody AdmissionReview admissionReview) {
        AdmissionRequest request = admissionReview.getRequest();
        AdmissionResponse response = new AdmissionResponse();
        response.setAllowed(true);
        response.setUid(request.getUid());

        try {
            String kind = request.getKind().getKind();
            String podName = request.getName();
            String namespace = request.getNamespace();

            logger.info("Processing request for kind: {} named: {} in namespace: {}", kind, podName, namespace);

            if (mutations != null && !mutations.isEmpty() && "Pod".equals(kind)) {
                Pod pod = objectMapper.convertValue(request.getObject(), Pod.class);
                String nodeName = pod.getSpec().getNodeName();

                logger.info("Processing request for Pod: {} in Namespace: {} on Node: {}", podName, namespace, nodeName);

                StringBuilder patchBuilder = new StringBuilder("[");
                boolean mutated = false;

                for (Map<String, String> mutation : mutations) {
                    String path = mutation.get("path");
                    String value = mutation.get("value");

                    if (path != null && value != null) {
                        if (mutated) {
                            patchBuilder.append(",");
                        }
                        patchBuilder.append("{\"op\":\"replace\",\"path\":\"")
                                .append(path)
                                .append("\",\"value\":\"")
                                .append(value)
                                .append("\"}");
                        mutated = true;
                    } else {
                        logger.warn("Invalid mutation format: {}", mutation);
                    }
                }
                patchBuilder.append("]");

                if (mutated) {
                    response.setPatchType("JSONPatch");
                    response.setPatch(Base64.getEncoder().encodeToString(patchBuilder.toString().getBytes()));
                    logger.info("Mutation applied successfully to Pod: {}", podName);
                }
            } else {
                logger.info("No mutations applied. Object kind: {}", kind);
            }
        } catch (Exception e) {
            logger.error("Error during mutation", e);
            response.setAllowed(false);
            response.setStatus(new Status());
            response.getStatus().setMessage("Error during mutation: " + e.getMessage());
        }

        admissionReview.setResponse(response);
        return admissionReview;
    }
}