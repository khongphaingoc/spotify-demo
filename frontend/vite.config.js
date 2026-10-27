import {defineConfig} from 'vite';
export default defineConfig({server:{port:15173,strictPort:true,proxy:{'/api':'http://127.0.0.1:18080','/audio':'http://127.0.0.1:18080'}}});
