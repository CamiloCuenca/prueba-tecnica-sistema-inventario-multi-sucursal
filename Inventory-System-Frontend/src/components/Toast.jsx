import { Toaster } from 'sonner';

export default function Toast() {
  return <Toaster position="top-right" richColors closeButton expand toastOptions={{ duration: 3500 }} />;
}