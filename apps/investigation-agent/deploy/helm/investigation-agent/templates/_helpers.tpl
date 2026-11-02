{{- define "investigation-agent.name" -}}
investigation-agent
{{- end }}

{{- define "investigation-agent.fullname" -}}
{{ include "investigation-agent.name" . }}
{{- end }}

{{- define "investigation-agent.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
    {{- default (include "investigation-agent.fullname" .) .Values.serviceAccount.name }}
{{- else }}
    {{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{- define "investigation-agent.labels" -}}
app.kubernetes.io/name: investigation-agent
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}