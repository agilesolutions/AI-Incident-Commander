{{- define "incident-service.name" -}}
incident-service
{{- end }}

{{- define "incident-service.fullname" -}}
{{ include "incident-service.name" . }}
{{- end }}

{{- define "incident-service.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
    {{- default (include "incident-service.fullname" .) .Values.serviceAccount.name }}
{{- else }}
    {{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{- define "incident-service.labels" -}}
app.kubernetes.io/name: incident-service
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}