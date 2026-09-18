/*
 * Copyright (C) 2021, 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <aidl/android/hardware/power/BnPower.h>
#include <aidl/vendor/xiaomi/hw/touchfeature/ITouchFeature.h>
#include <android-base/logging.h>
#include <android/binder_manager.h>

#include <cstdint>
#include <memory>
#include <mutex>

using aidl::vendor::xiaomi::hw::touchfeature::ITouchFeature;

namespace {

constexpr int32_t kPrimaryTouchId = 0;
constexpr int32_t kTouchModeDoubleTap = 14;
constexpr char kTouchFeatureService[] = "vendor.xiaomi.hw.touchfeature.ITouchFeature/default";

std::mutex gTouchLock;
std::shared_ptr<ITouchFeature> gTouchFeature;

bool setDoubleTapMode(bool enabled) {
    std::lock_guard<std::mutex> lock(gTouchLock);
    if (gTouchFeature == nullptr) {
        ndk::SpAIBinder binder(AServiceManager_waitForService(kTouchFeatureService));
        gTouchFeature = ITouchFeature::fromBinder(binder);
        if (gTouchFeature == nullptr) {
            LOG(ERROR) << "TouchFeature service is unavailable";
            return false;
        }
    }

    auto status = gTouchFeature->setTouchMode(kPrimaryTouchId, kTouchModeDoubleTap, enabled ? 1 : 0);
    if (!status.isOk()) {
        LOG(ERROR) << "Failed to set double-tap touch mode: " << status.getDescription();
        gTouchFeature.reset();
        return false;
    }
    return true;
}

}  // namespace

namespace aidl {
namespace android {
namespace hardware {
namespace power {
namespace impl {

using ::aidl::android::hardware::power::Mode;

bool isDeviceSpecificModeSupported(Mode type, bool* _aidl_return) {
    switch (type) {
        case Mode::DOUBLE_TAP_TO_WAKE:
            *_aidl_return = true;
            return true;
        default:
            return false;
    }
}

bool setDeviceSpecificMode(Mode type, bool enabled) {
    switch (type) {
        case Mode::DOUBLE_TAP_TO_WAKE:
            setDoubleTapMode(enabled);
            return true;
        default:
            return false;
    }
}

}  // namespace impl
}  // namespace power
}  // namespace hardware
}  // namespace android
}  // namespace aidl
