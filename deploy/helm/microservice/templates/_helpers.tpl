{{- define "microservice.name" -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "microservice.labels" -}}
{{ include "microservice.selectorLabels" . }}
app.kubernetes.io/version: {{ include "microservice.imageTag" . | quote }}
app.kubernetes.io/part-of: mediahub
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version }}
{{- end -}}

{{- define "microservice.selectorLabels" -}}
app.kubernetes.io/name: {{ include "microservice.name" . }}
{{- end -}}

{{- define "microservice.imageTag" -}}
{{- .Values.image.tag | default .Chart.AppVersion -}}
{{- end -}}
