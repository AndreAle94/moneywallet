/*
 * Copyright (c) 2018.
 *
 * This file is part of MoneyWallet.
 *
 * MoneyWallet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MoneyWallet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MoneyWallet.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.oriondev.moneywallet.broadcast;

/**
 * Created by andre on 22/03/2018.
 */
public class LocalAction {

    public static final String ACTION_BACKUP_SERVICE_STARTED = "LocalBroadCast::BackupServiceStarted";
    public static final String ACTION_BACKUP_SERVICE_RUNNING = "LocalBroadCast::BackupServiceRunning";
    public static final String ACTION_BACKUP_SERVICE_FINISHED = "LocalBroadCast::BackupServiceFinished";
    public static final String ACTION_BACKUP_SERVICE_FAILED = "LocalBroadCast::BackupServiceFailed";
    public static final String ACTION_BACKEND_SERVICE_STARTED = "LocalBroadCast::BackendServiceStarted";
    public static final String ACTION_BACKEND_SERVICE_RUNNING = "LocalBroadCast::BackendServiceRunning";
    public static final String ACTION_BACKEND_SERVICE_FINISHED = "LocalBroadCast::BackendServiceFinished";
    public static final String ACTION_BACKEND_SERVICE_FAILED = "LocalBroadCast::BackendServiceFailed";
    public static final String ACTION_EXCHANGE_RATES_UPDATED = "LocalBroadCast::ExchangeRatesUpdated";
    public static final String ACTION_ATTACHMENT_OP_STARTED = "LocalBroadCast::AttachmentOperationStarted";
    public static final String ACTION_ATTACHMENT_OP_FINISHED = "LocalBroadCast::AttachmentOperationFinished";
    public static final String ACTION_ATTACHMENT_OP_FAILED = "LocalBroadCast::AttachmentOperationFailed";
    public static final String ACTION_LEGACY_EDITION_UPGRADE_STARTED = "LocalBroadCast::LegacyEditionUpgradeStarted";
    public static final String ACTION_LEGACY_EDITION_UPGRADE_FINISHED = "LocalBroadCast::LegacyEditionUpgradeFinished";
    public static final String ACTION_LEGACY_EDITION_UPGRADE_FAILED = "LocalBroadCast::LegacyEditionUpgradeFailed";
    public static final String ACTION_IMPORT_SERVICE_STARTED = "LocalBroadCast::ImportServiceStarted";
    public static final String ACTION_IMPORT_SERVICE_FINISHED = "LocalBroadCast::ImportServiceFinished";
    public static final String ACTION_IMPORT_SERVICE_FAILED = "LocalBroadCast::ImportServiceFailed";
    public static final String ACTION_EXPORT_SERVICE_STARTED = "LocalBroadCast::ExportServiceStarted";
    public static final String ACTION_EXPORT_SERVICE_FINISHED = "LocalBroadCast::ExportServiceFinished";
    public static final String ACTION_EXPORT_SERVICE_FAILED = "LocalBroadCast::ExportServiceFailed";

    public static final String ACTION_ITEM_CLICK = "LocalBroadCast::ItemClicked";

    public static final String ACTION_CURRENT_WALLET_CHANGED = "LocalBroadCast::CurrentWalletChanged";
    public static final String ARGUMENT_WALLET_ID = "LocalBroadCast::WalletId";
}