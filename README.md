# DMZ ReStart Plugin 🚀
**Professional Minecraft Server Management System**

[![Build Status](https://github.com/YourUsername/DMZ-ReStart/workflows/Build/badge.svg)](https://github.com/YourUsername/DMZ-ReStart/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-8+-blue.svg)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.13+-green.svg)](https://www.minecraft.net/)

## 🎯 Features

- **🔄 Smart Restart Management** - Automated and manual restart scheduling
- **⚡ Emergency Detection** - TPS/Memory monitoring with automatic emergency restarts
- **📊 Performance Analytics** - Real-time server performance monitoring with trend analysis
- **🔔 Advanced Alert System** - Boss bars, sounds, and visual notifications
- **🎮 Complete Command Interface** - Full command system with tab completion
- **📈 PlaceholderAPI Integration** - 30+ placeholders for data display
- **🛡️ Permission System** - Hierarchical permissions with LuckPerms support
- **📁 Professional Logging** - File logging with automatic rotation
- **📊 Metrics Collection** - CSV data export for performance analysis

## 🚀 Quick Start

### Requirements
- Java 8+
- Bukkit/Spigot/Paper 1.13+
- Maven (for building)

### Installation
1. Download the latest release from [Releases](https://github.com/YourUsername/DMZ-ReStart/releases)
2. Place `DMZ-ReStart-X.X.X.jar` in your `plugins/` folder
3. Restart your server
4. Configure `plugins/DMZ-ReStart/config.yml` as needed

### Building from Source
```bash
git clone https://github.com/YourUsername/DMZ-ReStart.git
cd DMZ-ReStart
mvn clean package
```

## 📋 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/dmzrestart help` | `dmzrestart.status` | Show help message |
| `/dmzrestart status` | `dmzrestart.status` | Show restart status |
| `/dmzrestart restart [delay]` | `dmzrestart.restart` | Schedule server restart |
| `/dmzrestart schedule <time>` | `dmzrestart.schedule` | Schedule restart at time |
| `/dmzrestart cancel` | `dmzrestart.cancel` | Cancel scheduled restarts |
| `/dmzrestart reload` | `dmzrestart.reload` | Reload configuration |

## 🔗 PlaceholderAPI

Over 30 placeholders available! Examples:
- `%dmzrestart_tps%` - Current server TPS
- `%dmzrestart_memory_usage%` - Memory usage percentage  
- `%dmzrestart_next_restart%` - Next scheduled restart
- `%dmzrestart_server_healthy%` - Server health status

[View all placeholders](https://github.com/YourUsername/DMZ-ReStart/wiki/PlaceholderAPI)

## ⚙️ Configuration

Default configuration with smart defaults:
```yaml
# Performance Monitoring
monitoring:
  enabled: true
  tps-threshold: 16.0
  memory-threshold: 85.0

# Emergency Restart
emergency:
  enabled: true
  tps-threshold: 12.0
  memory-threshold: 95.0

# Scheduled Restarts
restart-times:
  - "04:00"
  - "12:00" 
  - "20:00"
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- 📖 [Wiki](https://github.com/YourUsername/DMZ-ReStart/wiki)
- 🐛 [Issues](https://github.com/YourUsername/DMZ-ReStart/issues)
- 💬 [Discussions](https://github.com/YourUsername/DMZ-ReStart/discussions)

## 📊 Statistics

- **Lines of Code:** 3,000+
- **Features:** 25+ professional features
- **Compatibility:** Bukkit/Spigot/Paper 1.13+
- **Dependencies:** Zero required, PlaceholderAPI optional

---

**Made with ❤️ for the Minecraft community**
