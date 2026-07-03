import axios from 'axios'

export function formatTemplateDownloadError(error: unknown, fallback: string) {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status
    const message = error.message || fallback

    if (status === 502) {
      return `${fallback}：后端网关返回 502，请检查后端服务是否启动，以及 Nginx 的 /api 代理是否指向正确后端端口。`
    }

    if (error.code === 'ECONNABORTED' || message.toLowerCase().includes('timeout')) {
      return `${fallback}：请求超时，请检查后端服务、数据库连接和网络是否正常。`
    }

    if (!error.response) {
      return `${fallback}：无法连接后端接口，请检查后端是否启动、接口地址是否正确，或当前网络是否可访问服务器。`
    }

    return `${fallback}：${message}`
  }

  if (error instanceof Error) {
    return error.message || fallback
  }

  return fallback
}
