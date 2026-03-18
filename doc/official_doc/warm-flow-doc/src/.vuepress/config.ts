import {defineUserConfig} from "vuepress";

import {viteBundler} from '@vuepress/bundler-vite'

import theme from "./theme.js";

export default defineUserConfig({
    base: "/",
    port: 8081,
    locales: {
        "/": {
            lang: "zh-CN",
            title: "Warm-Flow官网",
        },
    },
    theme,
    shouldPrefetch: false,
    bundler: viteBundler({
        viteOptions: {},
        vuePluginOptions: {},
    }),
    dest: "./src/.vuepress/warm-flow-docs",
    head: [
        ['script', {}, `       
            var _hmt = _hmt || [];
            (function() {
              var hm = document.createElement("script");
              hm.src = "https://hm.baidu.com/hm.js?6ef49c07f07e726a81e746e27e746dfd";
              var s = document.getElementsByTagName("script")[0]; 
              s.parentNode.insertBefore(hm, s);
            })();
        `],
        ['script', { type: 'text/javascript', charset: 'UTF-8'
            , src: 'https://cdn.wwads.cn/js/makemoney.js', async: '' }, ''
        ],
    ],
});

