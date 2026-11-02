{{- define "remediation-service.name" -}}
remediation-service
{{- end }}

{{- define "remediation-service.fullname" -}}
{{ include "remediation-service.name" . }}
{{- end }}

{{- define "remediation-service.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
    {{- default (include "remediation-service.fullname" .) .Values.serviceAccount.name }}
{{- else }}
    {{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{- define "remediation-service.labels" -}}
app.kubernetes.io/name: remediation-service
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}