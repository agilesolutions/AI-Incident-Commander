{{- define "chaos-simulator.name" -}}
chaos-simulator
{{- end }}

{{- define "chaos-simulator.fullname" -}}
{{ include "chaos-simulator.name" . }}
{{- end }}

{{- define "chaos-simulator.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
    {{- default (include "chaos-simulator.fullname" .) .Values.serviceAccount.name }}
{{- else }}
    {{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{- define "chaos-simulator.labels" -}}
app.kubernetes.io/name: chaos-simulator
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}