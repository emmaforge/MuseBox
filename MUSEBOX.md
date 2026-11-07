# MuseBox — NekoBox for Android, adapted for musess

MuseBox 是 [NekoBox for Android](https://github.com/MatsuriDayo/NekoBoxForAndroid)
（MatsuriDayo，基于 sing-box 的安卓通用代理客户端）的改版：
把核心换成带 **musess 协议**的 sing-box，并在 App 里加入 musess 节点的导入选项
（`musess://` 链接一键导入，也可在节点列表里新建 musess 节点）。

musess 协议本身见 [emmaforge/musess](https://github.com/emmaforge/musess)：
强伪装、低延迟、丢包不卡、跑满带宽；借站直通（Wraith）。

## 状态

- 初始提交：NekoBoxForAndroid **1.4.2** 源码原样导入（见 `README.md`）。
- 进行中：核心合入 musess 协议（MatsuriDayo/sing-box `1.12.x` 分支），
  App 侧加入 musess Bean / 链接解析 / 出站配置生成 / 类型注册 / 编辑界面。

## 构建

与上游一致，另需：

- 兄弟目录 `sing-box`：MatsuriDayo/sing-box `1.12.x`（已合入 musess 协议），
  `libneko`：MatsuriDayo/libneko；见 `buildScript/lib/core/get_source.sh`。
- Android NDK `25.0.8775105`，gomobile-matsuri（见 `libcore/init.sh`）。

## 签名

本仓库**不含**上游的 `release.keystore`（签名私钥不能公开）。
自己打包发布时请生成自己的签名密钥。

## 许可证

GPL-3.0（见 `LICENSE`，上游版权归 nekohasekai / SagerNet 所有；
本改版同样遵循 GPL-3.0 开源）。
