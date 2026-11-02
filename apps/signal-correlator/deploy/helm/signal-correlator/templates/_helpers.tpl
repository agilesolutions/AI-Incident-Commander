{{- define "signal-correlator.name" -}}
signal-correlator
{{- end }}

{{- define "signal-correlator.fullname" -}}
{{ include "signal-correlator.name" . }}
{{- end }}

{{- define "signal-correlator.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
    {{- default (include "signal-correlator.fullname" .) .Values.serviceAccount.name }}
{{- else }}
    {{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{- define "signal-correlator.labels" -}}
app.kubernetes.io/name: signal-correlator
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}